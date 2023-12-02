package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MatchUtil;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.response.Response;
import com.hamsterhub.service.FileService;
import com.hamsterhub.service.dto.*;
import com.hamsterhub.service.service.*;
import com.hamsterhub.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@Api(tags = "文件传输 数据接口")
public class FileController {

    @Autowired
    private RFileService rFileService;
    @Autowired
    private VFileService vFileService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private StrategyService strategyService;
    @Autowired
    private ShareService shareService;
    @Autowired
    private FileService fileService;

    @ApiOperation("查询文件是否存在(token)")
    @GetMapping(value = "/isExist")
    @Token
    public Response isExist(@RequestParam("root") String root,
                            @RequestParam("hash") String hash) {
        StrategyDTO strategyDTO = strategyService.query(root);
        return Response.success().data(rFileService.isExist(hash, strategyDTO.getId()));
    }

    @ApiOperation("查看文件列表(token)")
    @GetMapping(value = "/queryFile")
    @Token
    public Response queryFile(@RequestParam("root") String root,
                              @RequestParam("url") String url) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 路径格式错误
        if (!MatchUtil.isPathMatches(url))
            throw new BusinessException(CommonErrorCode.E_600002);
        // 分割路径和文件名
        String path = url,
               name = "";
        if (!path.equals("/"))
            name = path.substring(path.lastIndexOf('/') + 1);
        path = path.substring(0, path.lastIndexOf('/'));
        if (path.equals("")) path = "/";
        VFileDTO vFileDTO = new VFileDTO();
        if (!name.equals("")) { // 不是根目录
            vFileDTO = vFileService.query(accountDTO.getId(), root, path, name);
            // 文件不存在
            if (vFileDTO == null)
                throw new BusinessException(CommonErrorCode.E_600001);
        }
        if (name.equals("") || vFileDTO.getType().equals(0)) { // 文件夹
            if (path.charAt(path.length() - 1) != '/') path += "/";
            return Response.success().data(vFileService.queryBatch(accountDTO.getId(), root, path + name));
        }
        else
            return Response.success().data(vFileDTO);
    }

    @ApiOperation("创建目录(token)")
    @PostMapping(value = "/mkdir")
    @Token
    public Response mkdir(@RequestParam("root") String root,
                          @RequestParam("url") String url) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 路径格式错误
        if (!MatchUtil.isPathMatches(url))
            throw new BusinessException(CommonErrorCode.E_600002);

        // 遍历创建目录
        String[] dirs = url.split("/");
        String path = "/";
        StrategyDTO strategyDTO = strategyService.query(root);
        for (int i = 0; i < dirs.length; i ++) {
            String name = dirs[i];
            if (name.equals("")) continue;
            if (!vFileService.isExist(accountDTO.getId(), root, path, name))
                vFileService.create(new VFileDTO(null, 0, name, path, 0L, 0, LocalDateTime.now(), accountDTO.getId(), 0L, strategyDTO.getId()));
            if (path.equals("/")) path = "";
            path += "/" + name;
        }

        return Response.success();
    }

    @ApiOperation("上传文件(hash可选)(token)")
    @PostMapping(value = "/upload")
    @Token
    public Response upload(@RequestParam("root") String root,
                           @RequestParam("file") MultipartFile file,
                           @RequestParam(value = "hash", required = false) String hash,
                           @RequestParam("path") String path) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 路径格式错误
        if (!MatchUtil.isPathMatches(path))
            throw new BusinessException(CommonErrorCode.E_600002);
        // 分割路径和文件名
        String name = "";
        if (!path.equals("/"))
            name = path.substring(path.lastIndexOf('/') + 1);
        path = path.substring(0, path.lastIndexOf('/'));
        if (path.equals("")) path = "/";
        if (!name.equals("")) { // 不为根目录，检查目录是否存在
            VFileDTO vFileDTO = vFileService.query(accountDTO.getId(), root, path, name);
            if (vFileDTO == null || !vFileDTO.getType().equals(0))
                throw new BusinessException(CommonErrorCode.E_600003);
        }
        if (!name.equals("")) {
            if (!path.equals("/")) path += "/";
            path += name;
        }
        name = file.getOriginalFilename();
        // 文件名为空
        if (StringUtil.isBlank(name))
            throw new BusinessException(CommonErrorCode.E_600004);
        // 文件是否存在,存在则版本号+1
        Integer version = 1;
        VFileDTO vFileDTO = vFileService.query(accountDTO.getId(), root, path, name);
        if (vFileDTO != null)
            version = vFileDTO.getVersion() + 1;
        StrategyDTO strategyDTO = strategyService.query(root);
        // 存储文件
        RFileDTO rFileDTO = null;
        if (!StringUtil.isBlank(hash)) { // 上传已有文件
            if (!rFileService.isExist(hash, strategyDTO.getId()))
                throw new BusinessException(CommonErrorCode.E_500001);
            rFileDTO = rFileService.query(hash, strategyDTO.getId());
        }
        else { // 上传新文件
            rFileDTO = fileService.upload(file, strategyDTO);
        }

        vFileDTO = new VFileDTO(null, 1, name, path, rFileDTO.getId(), version, LocalDateTime.now(), accountDTO.getId(), rFileDTO.getSize(), strategyDTO.getId());
        vFileService.create(vFileDTO);

        return Response.success().msg("上传成功").data(vFileDTO);
    }

    @ApiOperation("下载文件(token)")
    @GetMapping(value = "/download")
    @Token
    public Response download(@RequestParam("vFileId") Long vFileId,
                             HttpServletResponse response) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        VFileDTO vFileDTO = vFileService.query(vFileId);
        // 文件与用户不匹配
        if (!vFileDTO.getAccountID().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600005);
        RFileDTO rFileDTO = rFileService.query(vFileDTO.getRFileId());
        DeviceDTO deviceDTO = deviceService.query(rFileDTO.getDeviceId());
        String result = fileService.download(rFileDTO);

        if (deviceDTO.getType() != 0) // 非本地硬盘，返回直链
            return Response.success().data(result);
        else // 设备本地硬盘，返回文件
            try {
                File file = new File(result);
                // 将文件写入输入流
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStream fis = new BufferedInputStream(fileInputStream);
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                fis.close();

                // 清空response
                response.reset();
                // 设置response的Header
                response.setCharacterEncoding("UTF-8");
                //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
                //attachment表示以附件方式下载   inline表示在线打开   "Content-Disposition: inline; filename=文件名.mp3"
                // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
                response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(vFileDTO.getName(), "UTF-8"));
                // 告知浏览器文件的大小
                response.addHeader("Content-Length", "" + file.length());
                OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
                response.setContentType("application/octet-stream");
                outputStream.write(buffer);
                outputStream.flush();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        return Response.success();
    }

    @ApiOperation("删除文件(token)")
    @PostMapping(value = "/delete")
    @Token
    public Response delete(@RequestParam("vFileId") Long vFileId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        VFileDTO vFileDTO = vFileService.query(vFileId);
        // 文件与用户不匹配
        if (!vFileDTO.getAccountID().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600005);

        List<Long> delete = vFileService.delete(vFileDTO.getId());
        for (Long i: delete)
            fileService.delete(rFileService.query(i));
        return Response.success();
    }

}
