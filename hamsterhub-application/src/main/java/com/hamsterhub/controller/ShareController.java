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

    @Operation(summary ="分享文件(token)")
    @PostMapping(value = "/share")
    @Token
    public Response shareFile(@RequestParam("vFileId") Long vFileId,
                              @RequestParam(value = "key", required = false) String key,
                              @RequestParam(value = "expiry", required = false) Long expiry,
                              @RequestParam(value = "name", required = false) String name) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        VFileDTO vFileDTO = vFileService.query(vFileId);
        // 文件与用户不匹配
        if (!vFileDTO.getAccountID().equals(accountDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_600005);
        // 该文件正在分享
        Long shareParent = vFileService.getShareParent(vFileId);
        if (!shareParent.equals(0L) && !shareParent.equals(2L))
            throw new BusinessException(CommonErrorCode.E_600006);

        // 0: 无需提取码 1: 需要提取码
        Integer type = 0;
        if (!StringUtil.isBlank(key))
            type = 1;
        else
            key = "";

        String ticket;
        do {
            ticket = StringUtil.generateRandomString(10);
        } while (shareService.isExist(ticket));

        LocalDateTime expiryTime;
        if (expiry == null)
            expiryTime = LocalDateTime.parse("9999-12-31T23:59:59");
        else
            expiryTime = LocalDateTime.now().plusSeconds(expiry);

        ShareDTO shareDTO = new ShareDTO(null, type, ticket, vFileId, key, expiryTime, accountDTO.getId(), name);
        ShareResponse data = ShareConvert.INSTANCE.dto2res(shareService.create(shareDTO));

        vFileDTO.setShareType(1);
        vFileService.update(vFileDTO);

        return Response.success().data(data);
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
        VFileDTO vFileDTO = vFileService.query(shareDTO.getVFileId());
        vFileDTO.setShareType(0);
        vFileService.update(vFileDTO);

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
    public Response queryShareFile(@RequestParam("ticket") String ticket,
                               @RequestParam(value = "key", required = false) String key,
                               @RequestParam(value = "vFileId", required = false) Long vFileId) {
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

        if (vFileId == null)
            vFileId = shareDTO.getVFileId();

        // vFileId与ticket不匹配
        Long shareParent = vFileService.getShareParent(vFileId);
        if (!shareParent.equals(shareDTO.getVFileId()))
            throw new BusinessException(CommonErrorCode.E_600020);

        VFileDTO vFileDTO = vFileService.query(vFileId);
        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileDTO);

        // 是目录则把文件数存入size字段
        if (data.getType().equals(0))
            data.setSize(vFileService.queryCount(vFileDTO.getId()).toString());

        return Response.success().data(data);
    }

    @Operation(summary ="获取分享文件")
    @GetMapping(value = "/searchShareFile")
    public Response searchShareFile(@RequestParam("ticket") String ticket,
                                   @RequestParam(value = "key", required = false) String key,
                                   @RequestParam("parentId") Long parentId,
                                    @RequestParam("name") String name) {
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

        // parentId与ticket不匹配
        Long shareParentId = vFileService.getShareParent(parentId);
        if (!shareParentId.equals(shareDTO.getVFileId()))
            throw new BusinessException(CommonErrorCode.E_600020);
        VFileDTO shareParent = vFileService.query(shareParentId);

        VFileDTO vFileDTO = vFileService.query(shareParent.getStrategyId(), parentId, name);
        VFileResponse data = VFileConvert.INSTANCE.dto2res(vFileDTO);

        // 是目录则把文件数存入size字段
        if (data.getType().equals(0))
            data.setSize(vFileService.queryCount(vFileDTO.getId()).toString());

        return Response.success().data(data);
    }

    @Operation(summary ="获取分享文件列表")
    @GetMapping(value = "/queryShareList")
    public Response queryList(@RequestParam("ticket") String ticket,
                              @RequestParam(value = "key", required = false) String key,
                              @RequestParam("parentId") Long parentId,
                              @RequestParam(value = "page", required = false) Integer page,
                              @RequestParam(value = "limit", required = false) Integer limit) {
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

        if (limit == null)
            limit = 10;

        // vFileId与ticket不匹配
        Long shareParent = vFileService.getShareParent(parentId);
        if (!shareParent.equals(shareDTO.getVFileId()))
            throw new BusinessException(CommonErrorCode.E_600020);

        List<VFileDTO> vFileDTOs;
        if (page == null) // 未分页
            vFileDTOs = vFileService.queryBatch(parentId);
        else
            vFileDTOs = vFileService.queryBatch(parentId, page, limit);

        List<VFileResponse> data = VFileConvert.INSTANCE.dto2resBatch(vFileDTOs);
        return Response.success().data(data);
    }

    @Operation(summary ="获取分享文件下载地址")
    @GetMapping(value = "/getShareDownloadUrl")
    public Response downloadShare(@RequestParam("ticket") String ticket,
                                  @RequestParam(value = "key", required = false) String key,
                                  @RequestParam(value = "vFileId", required = false) Long vFileId) {
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

        if (vFileId == null)
            vFileId = shareDTO.getVFileId();

        VFileDTO vFileDTO = vFileService.query(vFileId);
        RFileDTO rFileDTO = rFileService.query(vFileDTO.getRFileId());
        DeviceDTO deviceDTO = deviceService.query(rFileDTO.getDeviceId());

        String url;
        if (deviceDTO.getType().equals(0)) {// 本地硬盘
            FileLinkDTO fileLinkDTO;
            String fileTicket;
            if (fileLinkService.isExist(rFileDTO.getId())) { // 文件直链已存在
                fileLinkDTO = fileLinkService.query(rFileDTO.getId());
                if (fileLinkDTO.getExpiry().isBefore(LocalDateTime.now())) { // 直链已过期
                    do {
                        fileLinkDTO.setTicket(StringUtil.generateRandomString(10));
                    }
                    while (fileLinkService.isExist(fileLinkDTO.getTicket()));
                }
                fileLinkDTO.setExpiry(LocalDateTime.now().plusMinutes(10));
                fileLinkService.update(fileLinkDTO);
            }
            else {
                do {
                    fileTicket = StringUtil.generateRandomString(10);
                }
                while (fileLinkService.isExist(fileTicket));

                fileLinkDTO = new FileLinkDTO(fileTicket, rFileDTO.getId(), LocalDateTime.now().plusMinutes(10));
                fileLinkService.create(fileLinkDTO);
            }

            url = String.format("/download?ticket=%s&fileName=%s", fileLinkDTO.getTicket(), vFileDTO.getName());
        }
        else // 网盘
            url = fileService.download(rFileDTO);
        return Response.success().data(url);
    }



}
