package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
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

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@RestController
@Api(tags = "分享文件 数据接口")
public class ShareController {

    @Autowired
    private VFileService vFileService;
    @Autowired
    private RFileService rFileService;
    @Autowired
    private ShareService shareService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private FileService fileService;

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomString = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            randomString.append(characters.charAt(index));
        }

        return randomString.toString();
    }

    @ApiOperation("分享文件(token)")
    @PostMapping(value = "/shareFile")
    @Token
    public Response shareFile(@RequestParam("vFileId") Long vFileId, @RequestParam(value = "key", required = false) String key, @RequestParam(value = "expiry", required = false) Long expiry) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        VFileDTO vFileDTO = vFileService.query(vFileId);
        // 文件与用户不匹配
        if (!vFileDTO.getAccountID().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600005);
        // 该文件正在分享
        if (shareService.isExistByVFileId(vFileId))
            throw new BusinessException(CommonErrorCode.E_600006);

        // 0: 无需提取码 1: 需要提取码
        Integer type = 0;
        // 默认随机提取码
        if (!StringUtil.isBlank(key))
            type = 1;

        String ticket = generateRandomString(10);
        while (shareService.isExist(ticket))
            ticket = generateRandomString(10);
        ShareDTO shareDTO = new ShareDTO(null, type, ticket, vFileId, key, LocalDateTime.now().plusSeconds(expiry), accountDTO.getId());
        ShareDTO data = shareService.create(shareDTO);

        return Response.success().data(data);
    }

    @ApiOperation("自己分享的文件(token)")
    @GetMapping(value = "/queryShares")
    @Token
    public Response queryShares() {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        List<ShareDTO> shareDTOs = shareService.queryBatch(accountDTO.getId());
        return Response.success().data(shareDTOs);
    }

    @ApiOperation("获取分享文件")
    @PostMapping(value = "/queryShare")
    public Response queryShare(@RequestParam("ticket") String ticket, @RequestParam("key") String key) {
        // 分享码不存在
        if (!shareService.isExist(ticket))
            throw new BusinessException(CommonErrorCode.E_600007);

        ShareDTO shareDTO = shareService.query(ticket);

        // 文件分享已过期
        if (shareDTO.getExpiry().isBefore(LocalDateTime.now()))
            throw new BusinessException(CommonErrorCode.E_600010);

        // 需要提取码
        if (shareDTO.getType().equals(1))
            // 提取码为空
            if (StringUtil.isBlank(key))
                throw new BusinessException(CommonErrorCode.E_600008);
            // 提取码错误
            if (!key.equals(shareDTO.getKey()))
                throw new BusinessException(CommonErrorCode.E_600009);

        VFileDTO vFileDTO = vFileService.query(shareDTO.getVFileId());
        return Response.success().data(vFileDTO);
    }

    @ApiOperation("下载分享文件")
    @PostMapping(value = "/downloadShare")
    public Response downloadShare(@RequestParam("ticket") String ticket, @RequestParam("key") String key, HttpServletResponse response) {
        // 分享码不存在
        if (!shareService.isExist(ticket))
            throw new BusinessException(CommonErrorCode.E_600007);

        ShareDTO shareDTO = shareService.query(ticket);

        // 文件分享已过期
        if (shareDTO.getExpiry().isBefore(LocalDateTime.now()))
            throw new BusinessException(CommonErrorCode.E_600010);

        // 需要提取码
        if (shareDTO.getType().equals(1))
            // 提取码为空
            if (StringUtil.isBlank(key))
                throw new BusinessException(CommonErrorCode.E_600008);
        // 提取码错误
        if (!key.equals(shareDTO.getKey()))
            throw new BusinessException(CommonErrorCode.E_600009);

        VFileDTO vFileDTO = vFileService.query(shareDTO.getVFileId());
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

    @ApiOperation("取消分享文件(token)")
    @PostMapping(value = "/deleteShare")
    @Token
    public Response deleteShare(@RequestParam("shareId") Long shareId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 分享ID不存在
        if (!shareService.isExist(shareId))
            throw new BusinessException(CommonErrorCode.E_600011);
        ShareDTO shareDTO = shareService.query(shareId);
        // 分享与用户不匹配
        if (!shareDTO.getAccountID().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600012);

        shareService.delete(shareId);

        return Response.success();
    }



}
