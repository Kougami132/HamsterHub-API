package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.convert.ShareConvert;
import com.hamsterhub.convert.VFileConvert;
import com.hamsterhub.response.Response;
import com.hamsterhub.response.ShareResponse;
import com.hamsterhub.response.VFileResponse;
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

    @ApiOperation("分享文件(token)")
    @PostMapping(value = "/shareFile")
    @Token
    public Response shareFile(@RequestParam("vFileId") Long vFileId,
                              @RequestParam(value = "key", required = false) String key,
                              @RequestParam(value = "expiry", required = false) Long expiry) {
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
        if (!StringUtil.isBlank(key))
            type = 1;

        String ticket;
        do {
            ticket = StringUtil.generateRandomString(10);
        } while (shareService.isExist(ticket));

        ShareDTO shareDTO = new ShareDTO(null, type, ticket, vFileId, key, LocalDateTime.now().plusSeconds(expiry), accountDTO.getId());
        ShareResponse data = ShareConvert.INSTANCE.dto2res(shareService.create(shareDTO));

        return Response.success().data(data);
    }

    @ApiOperation("自己分享的文件(token)")
    @GetMapping(value = "/queryShares")
    @Token
    public Response queryShares() {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        List<ShareDTO> shareDTOs = shareService.queryBatch(accountDTO.getId());
        List<ShareResponse> data = ShareConvert.INSTANCE.dto2resBatch(shareDTOs);
        return Response.success().data(data);
    }

    @ApiOperation("获取分享文件")
    @GetMapping(value = "/queryShare")
    public Response queryShare(@RequestParam("ticket") String ticket,
                               @RequestParam("key") String key) {
        // 分享码不存在
        if (!shareService.isExist(ticket))
            throw new BusinessException(CommonErrorCode.E_600007);

        ShareDTO shareDTO = shareService.query(ticket);

        // 文件分享已过期
        if (shareDTO.getExpiry().isBefore(LocalDateTime.now()))
            throw new BusinessException(CommonErrorCode.E_600010);

        // 需要提取码
        if (shareDTO.getType().equals(1)) {
            // 提取码为空
            if (StringUtil.isBlank(key))
                throw new BusinessException(CommonErrorCode.E_600008);
            // 提取码错误
            if (!key.equals(shareDTO.getKey()))
                throw new BusinessException(CommonErrorCode.E_600009);
        }

        VFileDTO vFileDTO = vFileService.query(shareDTO.getVFileId());
        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileDTO);
        return Response.success().data(data);
    }

    @ApiOperation("下载分享文件")
    @GetMapping(value = "/downloadShare")
    public Response downloadShare(@RequestParam("ticket") String ticket,
                                  @RequestParam(value = "key", required = false) String key,
                                  HttpServletResponse response) {
        // 分享码不存在
        if (!shareService.isExist(ticket))
            throw new BusinessException(CommonErrorCode.E_600007);

        ShareDTO shareDTO = shareService.query(ticket);

        // 文件分享已过期
        if (shareDTO.getExpiry().isBefore(LocalDateTime.now()))
            throw new BusinessException(CommonErrorCode.E_600010);

        // 需要提取码
        if (shareDTO.getType().equals(1)) {
            // 提取码为空
            if (StringUtil.isBlank(key))
                throw new BusinessException(CommonErrorCode.E_600008);
            // 提取码错误
            if (!key.equals(shareDTO.getKey()))
                throw new BusinessException(CommonErrorCode.E_600009);
        }

        VFileDTO vFileDTO = vFileService.query(shareDTO.getVFileId());
        RFileDTO rFileDTO = rFileService.query(vFileDTO.getRFileId());
        DeviceDTO deviceDTO = deviceService.query(rFileDTO.getDeviceId());

        String url;
        if (deviceDTO.getType().equals(0)) // 本地硬盘
            url = String.format("/download?rFileId=%s&fileName=%s", rFileDTO.getId(), vFileDTO.getName());
        else // 网盘
            url = fileService.download(rFileDTO);
        return Response.success().data(url);
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

        return Response.success().msg("分享取消成功");
    }



}
