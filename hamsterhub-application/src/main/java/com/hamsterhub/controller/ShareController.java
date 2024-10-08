package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.convert.ShareConvert;
import com.hamsterhub.convert.VFileConvert;
import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.database.dto.ShareDTO;
import com.hamsterhub.database.dto.VFileDTO;
import com.hamsterhub.database.service.ShareService;
import com.hamsterhub.database.service.VFileService;
import com.hamsterhub.response.Response;
import com.hamsterhub.response.ShareResponse;
import com.hamsterhub.response.VFileResponse;
import com.hamsterhub.service.service.*;
import com.hamsterhub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@Tag(name = "分享文件 数据接口")
@RequestMapping("api")
public class ShareController {

    @Autowired
    private VFileService vFileService;
    @Autowired
    private ShareService shareService;
    @Autowired
    private ShareFileStorageService shareFileStorageService;

    @Operation(summary ="分享文件(token)")
    @PostMapping(value = "/share")
    @Token
    public Response shareFile(@RequestParam("vFileId") String index,
                              @RequestParam("root") String root,
                              @RequestParam(value = "key", required = false) String key,
                              @RequestParam(value = "expiry", required = false) Long expiry,
                              @RequestParam(value = "name", required = false) String name
    ) throws UnsupportedEncodingException {

        UserDTO userDTO = SecurityUtil.getUser();
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(index),CommonErrorCode.E_100001);
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(root),CommonErrorCode.E_100001);
        index = URLDecoder.decode(index, StandardCharsets.UTF_8.name());
        ShareDTO shareDTO = shareFileStorageService.shareFile(root, index, userDTO, key, expiry, name);
        return Response.success().data(shareDTO);
    }

    @Operation(summary ="隐藏文件(token)")
    @PostMapping(value = "/hide")
    @Token
    public Response hide(@RequestParam("vFileId") Long vFileId) {
        UserDTO userDTO = SecurityUtil.getUser();
        VFileDTO vFileDTO = vFileService.query(vFileId);
        // 文件与用户不匹配
        if (!vFileDTO.getUserId().equals(userDTO.getId()))
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
        UserDTO userDTO = SecurityUtil.getUser();
        VFileDTO vFileDTO = vFileService.query(vFileId);
        // 文件与用户不匹配
        if (!vFileDTO.getUserId().equals(userDTO.getId()))
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
        UserDTO userDTO = SecurityUtil.getUser();
        // 参数为空
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(root),CommonErrorCode.E_100001);
        shareFileStorageService.deleteShare(root, shareId, userDTO);

        return Response.success().msg("分享取消成功");
    }

    @Operation(summary ="自己分享的文件(token)")
    @GetMapping(value = "/queryShares")
    @Token
    public Response queryShares() {
        UserDTO userDTO = SecurityUtil.getUser();
        List<ShareDTO> shareDTOs = shareService.queryBatch(userDTO.getId());
        List<ShareResponse> data = ShareConvert.INSTANCE.dto2resBatch(shareDTOs);
        return Response.success().data(data);
    }

    @Operation(summary ="获取分享文件")
    @GetMapping(value = "/queryShareFile")
    public Response queryShareFile( @RequestParam("ticket") String ticket,
                               @RequestParam(value = "key", required = false) String key,
                               @RequestParam(value = "vFileId", required = false) String index
    ) throws UnsupportedEncodingException {

        CommonErrorCode.checkAndThrow(StringUtil.isBlank(ticket),CommonErrorCode.E_100001);

        if (StringUtil.isNotBlank(index)){
            index = URLDecoder.decode(index, StandardCharsets.UTF_8.name());
        }

        VFileDTO vFileDTO = shareFileStorageService.queryShareFile(ticket, key, index);
        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileDTO);
        return Response.success().data(data);
    }

    @Operation(summary ="获取分享文件")
    @GetMapping(value = "/searchShareFile")
    public Response searchShareFile(@RequestParam("ticket") String ticket,
                                   @RequestParam(value = "key", required = false) String key,
                                   @RequestParam("parentId") String parentIndex,
                                    @RequestParam("name") String name
    ) throws UnsupportedEncodingException {

        CommonErrorCode.checkAndThrow(StringUtil.isBlank(ticket),CommonErrorCode.E_100001);
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(parentIndex),CommonErrorCode.E_100001);
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(name),CommonErrorCode.E_100001);

        parentIndex = URLDecoder.decode(parentIndex, StandardCharsets.UTF_8.name());
        name = URLDecoder.decode(name, StandardCharsets.UTF_8.name());

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
                              @RequestParam(value = "limit", required = false) Integer limit
    ) throws UnsupportedEncodingException {

        CommonErrorCode.checkAndThrow(StringUtil.isBlank(ticket),CommonErrorCode.E_100001);
        CommonErrorCode.checkAndThrow(StringUtil.isBlank(parentIndex),CommonErrorCode.E_100001);

        parentIndex = URLDecoder.decode(parentIndex, StandardCharsets.UTF_8.name());
        List<VFileDTO> vFileDTOs = shareFileStorageService.queryList(ticket, key, parentIndex, page, limit);
        List<VFileResponse> data = VFileConvert.INSTANCE.dto2resBatch(vFileDTOs);
        return Response.success().data(data);
    }

    @Operation(summary ="获取分享文件下载地址")
    @GetMapping(value = "/getShareDownloadUrl")
    public Response downloadShare(@RequestParam("ticket") String ticket,
                                  @RequestParam(value = "key", required = false) String key,
                                  @RequestParam(value = "vFileId", required = false) String fileIndex,
                                  @RequestParam(value = "preference", required = false) Long preference
    ) throws UnsupportedEncodingException {

        CommonErrorCode.checkAndThrow(StringUtil.isBlank(ticket),CommonErrorCode.E_100001);

        if (StringUtil.isNotBlank(fileIndex)){
            fileIndex = URLDecoder.decode(fileIndex, StandardCharsets.UTF_8.name());
        }

        String url = shareFileStorageService.downloadShare(ticket, key, fileIndex, preference);
        return Response.success().data(url);
    }



}
