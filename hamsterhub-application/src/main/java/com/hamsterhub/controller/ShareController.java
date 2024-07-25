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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "分享文件 数据接口")
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
    private FileLinkService fileLinkService;
    @Autowired
    private FileService fileService;
    @Autowired
    private ShareFileStorageService shareFileStorageService;

    @Operation(summary ="分享文件(token)")
    @PostMapping(value = "/share")
    @Token
    public Response shareFile(@RequestParam("vFileId") String index,
                              @RequestParam("root") String root,
                              @RequestParam(value = "key", required = false) String key,
                              @RequestParam(value = "expiry", required = false) Long expiry,
                              @RequestParam(value = "name", required = false) String name) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(index),CommonErrorCode.E_100001);
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(root),CommonErrorCode.E_100001);
        ShareDTO shareDTO = shareFileStorageService.shareFile(root, index, accountDTO, key, expiry, name);
        return Response.success().data(shareDTO);
    }

    @Operation(summary ="隐藏文件(token)")
    @PostMapping(value = "/hide")
    @Token
    public Response hide(@RequestParam("vFileId") Long vFileId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        VFileDTO vFileDTO = vFileService.query(vFileId);
        // 文件与用户不匹配
        if (!vFileDTO.getAccountID().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600005);
        // 该文件正在分享
        if (shareService.isExistByVFileId(vFileId))
            throw new BusinessException(CommonErrorCode.E_600021);

        vFileDTO.setShareType(2);
        vFileService.update(vFileDTO);
        return Response.success().msg("隐藏成功");
    }

    @Operation(summary ="显示文件(token)")
    @PostMapping(value = "/show")
    @Token
    public Response show(@RequestParam("vFileId") Long vFileId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        VFileDTO vFileDTO = vFileService.query(vFileId);
        // 文件与用户不匹配
        if (!vFileDTO.getAccountID().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600005);
        // 该文件正在分享
        if (shareService.isExistByVFileId(vFileId))
            throw new BusinessException(CommonErrorCode.E_600021);

        vFileDTO.setShareType(0);
        vFileService.update(vFileDTO);
        return Response.success().msg("显示成功");
    }

    @Operation(summary ="取消分享文件(token)")
    @PostMapping(value = "/deleteShare")
    @Token
    public Response deleteShare(@RequestParam("root") String root,
                                @RequestParam("shareId") Long shareId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 参数为空
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(root),CommonErrorCode.E_100001);
        shareFileStorageService.deleteShare(root, shareId, accountDTO);

        return Response.success().msg("分享取消成功");
    }

    @Operation(summary ="自己分享的文件(token)")
    @GetMapping(value = "/queryShares")
    @Token
    public Response queryShares() {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        List<ShareDTO> shareDTOs = shareService.queryBatch(accountDTO.getId());
        List<ShareResponse> data = ShareConvert.INSTANCE.dto2resBatch(shareDTOs);
        return Response.success().data(data);
    }

    @Operation(summary ="获取分享文件")
    @GetMapping(value = "/queryShareFile")
    public Response queryShareFile( @RequestParam("ticket") String ticket,
                               @RequestParam(value = "key", required = false) String key,
                               @RequestParam(value = "vFileId", required = false) String index) {

        VFileDTO vFileDTO = shareFileStorageService.queryShareFile(ticket, key, index);
        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileDTO);
        return Response.success().data(data);
    }

    @Operation(summary ="获取分享文件")
    @GetMapping(value = "/searchShareFile")
    public Response searchShareFile(@RequestParam("ticket") String ticket,
                                   @RequestParam(value = "key", required = false) String key,
                                   @RequestParam("parentId") String parentIndex,
                                    @RequestParam("name") String name) {

        VFileDTO vFileDTO = shareFileStorageService.searchShareFile(ticket, key, parentIndex, name);
        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileDTO);
        return Response.success().data(data);
    }

    @Operation(summary ="获取分享文件列表")
    @GetMapping(value = "/queryShareList")
    public Response queryList(@RequestParam("ticket") String ticket,
                              @RequestParam(value = "key", required = false) String key,
                              @RequestParam("parentId") String parentIndex,
                              @RequestParam(value = "page", required = false) Integer page,
                              @RequestParam(value = "limit", required = false) Integer limit) {
        List<VFileDTO> vFileDTOs = shareFileStorageService.queryList(ticket, key, parentIndex, page, limit);
        List<VFileResponse> data = VFileConvert.INSTANCE.dto2resBatch(vFileDTOs);
        return Response.success().data(data);
    }

    @Operation(summary ="获取分享文件下载地址")
    @GetMapping(value = "/getShareDownloadUrl")
    public Response downloadShare(@RequestParam("ticket") String ticket,
                                  @RequestParam(value = "key", required = false) String key,
                                  @RequestParam(value = "vFileId", required = false) String fileIndex,
                                  @RequestParam(value = "preference", required = false) Long preference){

        String url = shareFileStorageService.downloadShare(ticket, key, fileIndex, preference);
        return Response.success().data(url);
    }



}
