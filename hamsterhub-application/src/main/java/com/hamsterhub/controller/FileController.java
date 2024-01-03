package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MatchUtil;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.convert.VFileConvert;
import com.hamsterhub.response.Response;
import com.hamsterhub.response.VFileResponse;
import com.hamsterhub.service.FileService;
import com.hamsterhub.service.RedisService;
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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    private RedisService redisService;

    @ApiOperation("查询文件是否存在(token)")
    @GetMapping(value = "/isExist")
    @Token
    public Response isExist(@RequestParam("root") String root,
                            @RequestParam("hash") String hash) {
        StrategyDTO strategyDTO = strategyService.query(root);
        return Response.success().data(rFileService.isExist(hash, strategyDTO.getId()));
    }

    @ApiOperation("查看文件详情(token)")
    @GetMapping(value = "/queryFile")
    @Token
    public Response queryFile(@RequestParam("root") String root,
                              @RequestParam("url") String url) {

        AccountDTO accountDTO = SecurityUtil.getAccount();

        // 路径格式错误
        if (!MatchUtil.isPathMatches(url))
            throw new BusinessException(CommonErrorCode.E_600002);

        List<String> split = Arrays.asList(url.split("/"));
        Integer num = split.size() - 1;
        // 找到最深的有缓存的目录
        String path = "";
        Long vFileId = null;
        while (num >= 1) {
            path = "/" + split.subList(1, num + 1)
                              .stream()
                              .collect(Collectors.joining("/"));
            // 读取redis里缓存的ID
            vFileId = redisService.getFileId(root, accountDTO.getId(), path);
            if (vFileId != null) // 拿到缓存则跳出
                break;
            num --;
        }

        // 全路径无缓存
        if (vFileId == null) {
            vFileId = 0L;
            path = "";
        }
        num ++;
        VFileDTO vFileDTO = null;
        while (num <= split.size() - 1) {
            String name = split.get(num);
            vFileDTO = vFileService.query(accountDTO.getId(), root, vFileId, name);

            vFileId = vFileDTO.getId();

            path += "/" + name;
            // 把路径与ID键值对写入redis
            redisService.setFileId(root, accountDTO.getId(), path, vFileId);

            // 文件类型不是目录
            if (!vFileDTO.getType().equals(0)) {
                if (num == split.size() - 1)
                    break;
                else
                    throw new BusinessException(CommonErrorCode.E_600003);
            }

            num ++;
        }

        if (vFileDTO == null)
            vFileDTO = vFileService.query(vFileId);

        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileDTO);

        // 是目录则把文件数存入size字段
        if (data.getType().equals(0))
            data.setSize(vFileService.queryCount(vFileDTO.getId()).toString());

        return Response.success().data(data);
    }

    @ApiOperation("查看文件列表(token)")
    @GetMapping(value = "/queryList")
    @Token
    public Response queryList(@RequestParam("root") String root,
                              @RequestParam("parentId") Long parentId,
                              @RequestParam(value = "page", required = false) Integer page,
                              @RequestParam(value = "limit", required = false) Integer limit) {
        if (limit == null)
            limit = 10;

        AccountDTO accountDTO = SecurityUtil.getAccount();
        List<VFileDTO> vFileDTOs;
        if (page == null) // 未分页
            vFileDTOs = vFileService.queryBatch(accountDTO.getId(), root, parentId);
        else
            vFileDTOs = vFileService.queryBatch(accountDTO.getId(), root, parentId, page, limit);

        List<VFileResponse> data = VFileConvert.INSTANCE.dto2resBatch(vFileDTOs);
        return Response.success().data(data);
    }

    @ApiOperation("创建目录(token)")
    @PostMapping(value = "/mkdir")
    @Token
    public Response mkdir(@RequestParam("root") String root,
                          @RequestParam("parentId") Long parentId,
                          @RequestParam("name") String name) {
        AccountDTO accountDTO = SecurityUtil.getAccount();

        StrategyDTO strategyDTO = strategyService.query(root);
        VFileDTO vFileDTO = new VFileDTO(null, 0, name, parentId, 0L, 0, LocalDateTime.now(), LocalDateTime.now(), accountDTO.getId(), 0L, strategyDTO.getId());
        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileService.createDir(vFileDTO));

        return Response.success().data(data);
    }

    @ApiOperation("查询目录内文件数(token)")
    @GetMapping(value = "/queryFileCount")
    @Token
    public Response queryFileCount(@RequestParam("fileId") Long fileId) {
        return Response.success().data(vFileService.queryCount(fileId));
    }

    @ApiOperation("上传文件(hash可选)(token)")
    @PostMapping(value = "/upload")
    @Token
    public Response upload(@RequestParam("root") String root,
                           @RequestParam("parentId") Long parentId,
                           @RequestParam("file") MultipartFile file,
                           @RequestParam(value = "hash", required = false) String hash) {
        AccountDTO accountDTO = SecurityUtil.getAccount();

        String name = file.getOriginalFilename();
        // 文件名为空
        if (StringUtil.isBlank(name))
            throw new BusinessException(CommonErrorCode.E_600004);

        // 覆盖
        // 文件是否存在,存在则版本号+1
        Integer version = 1;
        if (vFileService.isExist(accountDTO.getId(), root, parentId, name))
            version = vFileService.query(accountDTO.getId(), root, parentId, name).getVersion() + 1;

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

        VFileDTO vFileDTO = new VFileDTO(null, 1, name, parentId, rFileDTO.getId(), version, LocalDateTime.now(), LocalDateTime.now(), accountDTO.getId(), rFileDTO.getSize(), strategyDTO.getId());
        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileService.create(vFileDTO));

        return Response.success().msg("上传成功").data(data);
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
        return Response.success().msg("文件删除成功");
    }

    @ApiOperation("重命名文件(token)")
    @PostMapping(value = "/rename")
    @Token
    public Response rename(@RequestParam("vFileId") Long vFileId,
                           @RequestParam("name") String name) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        VFileDTO vFileDTO = vFileService.query(vFileId);
        // 文件与用户不匹配
        if (!vFileDTO.getAccountID().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600005);

        vFileService.rename(vFileId, name);
        return Response.success().msg("重命名成功");
    }

}
