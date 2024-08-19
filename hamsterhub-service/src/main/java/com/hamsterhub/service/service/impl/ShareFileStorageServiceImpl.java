package com.hamsterhub.service.service.impl;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.database.service.*;
import com.hamsterhub.service.device.ListFiler;
import com.hamsterhub.database.dto.AccountDTO;
import com.hamsterhub.database.dto.ShareDTO;
import com.hamsterhub.database.dto.VFileDTO;
import com.hamsterhub.service.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShareFileStorageServiceImpl implements ShareFileStorageService {
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
    private FileStorageService fileStorageService;

    public static final Integer VIRTUAL_FILE_SYSTEM = 0;
    public static final Integer REALY_FILE_SYSTEM = 1;

    private String generateTicket(){
        String ticket;
        do {
            ticket = StringUtil.generateRandomString(10);
        } while (shareService.isExist(ticket));

        return ticket;
    }

    private LocalDateTime generateExpiryTime(Long expiry){
        LocalDateTime expiryTime;
        if (expiry == null)
            expiryTime = LocalDateTime.parse("9999-12-31T23:59:59");
        else
            expiryTime = LocalDateTime.now().plusSeconds(expiry);

        return expiryTime;
    }

    @Override
    public ShareDTO shareFile(String root, String index, AccountDTO accountDTO, String key, Long expiry, String name){
        ListFiler listFiler = fileStorageService.getListFiler(root);

        // 0: 无需提取码 1: 需要提取码
        Integer type = 0;
        if (!StringUtil.isBlank(key))
            type = 1;
        else
            key = "";


        VFileDTO vFileDTO = null;

        // 创建之前
        if(listFiler.getFileSystem().equals(VIRTUAL_FILE_SYSTEM)){
            Long fileIndex = Long.parseLong(index);
            vFileDTO = vFileService.query(fileIndex);

            // 文件与用户不匹配
            CommonErrorCode.checkAndThrow(!vFileDTO.getAccountID().equals(accountDTO.getId()),CommonErrorCode.E_600005);

            // 该文件正在分享
            Long shareParent = vFileService.getShareParent(fileIndex);
            CommonErrorCode.checkAndThrow(!shareParent.equals(0L) && !shareParent.equals(2L),CommonErrorCode.E_600006);
        }

        String ticket = generateTicket();
        LocalDateTime expiryTime = generateExpiryTime(expiry);

        ShareDTO shareDTO = new ShareDTO(null, type, ticket, index, key, expiryTime, accountDTO.getId(), name, root);
        shareDTO = shareService.create(shareDTO);

        // 创建之后，处理副作用
        if(listFiler.getFileSystem().equals(VIRTUAL_FILE_SYSTEM)){
            vFileDTO.setShareType(1);
            vFileService.update(vFileDTO);
        }

        return shareDTO;
    }

    @Override
    public Boolean deleteShare(String root, Long shareId, AccountDTO accountDTO){
        ListFiler listFiler = fileStorageService.getListFiler(root);

        // 分享ID不存在
        CommonErrorCode.checkAndThrow(!shareService.isExist(shareId),CommonErrorCode.E_600011);

        // 分享与用户不匹配
        ShareDTO shareDTO = shareService.query(shareId);
        CommonErrorCode.checkAndThrow(!shareDTO.getAccountID().equals(accountDTO.getId()),CommonErrorCode.E_600012);

        shareService.delete(shareId);

        // 处理副作用
        if(listFiler.getFileSystem().equals(VIRTUAL_FILE_SYSTEM)){
            VFileDTO vFileDTO = vFileService.query(Long.parseLong(shareDTO.getFileIndex()));
            vFileDTO.setShareType(0);
            vFileService.update(vFileDTO);
        }

        return true;
    }

    private ShareDTO shareCheck(String ticket, String key){
        ShareDTO shareDTO = shareService.query(ticket);

        // 分享码不存在
        if (shareDTO == null)
            throw new BusinessException(CommonErrorCode.E_600007);

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

        return shareDTO;
    }

    @Override
    public VFileDTO queryShareFile(String ticket, String key, String index){
        ShareDTO shareDTO = shareCheck(ticket, key);

        ListFiler listFiler = fileStorageService.getListFiler(shareDTO.getRoot());

        String fileIndex = index == null ? shareDTO.getFileIndex() : index;

        Integer fileSystem = listFiler.getFileSystem();
        VFileDTO vFileDTO = null;
        if (fileSystem.equals(VIRTUAL_FILE_SYSTEM)){
            Long vFileId = Long.parseLong(fileIndex);
            // vFileId与ticket不匹配
            Long shareParent = vFileService.getShareParent(vFileId);
            if (!shareParent.equals(Long.parseLong(shareDTO.getFileIndex())))
                throw new BusinessException(CommonErrorCode.E_600020);

            vFileDTO = vFileService.query(vFileId);

            // 是目录则把文件数存入size字段
            if (vFileDTO.getType().equals(0))
                vFileDTO.setSize(vFileService.queryCount(Long.parseLong(vFileDTO.getId()) ).longValue());
        } else if (fileSystem.equals(REALY_FILE_SYSTEM)){
            // 对于真实路径，获取的路径应当以分享的路径开头，防止越过权限控制
            if (!fileIndex.startsWith(shareDTO.getFileIndex())){
                throw new BusinessException(CommonErrorCode.E_600020);
            }

            vFileDTO = listFiler.queryFile(fileIndex,shareDTO.getAccountID()).get(0);
        }

        return vFileDTO;
    }

    @Override
    public VFileDTO searchShareFile(String ticket, String key, String parentIndex, String name){
        ShareDTO shareDTO = shareCheck(ticket, key);
        ListFiler listFiler = fileStorageService.getListFiler(shareDTO.getRoot());

        Integer fileSystem = listFiler.getFileSystem();
        VFileDTO vFileDTO = null;

        if (fileSystem.equals(VIRTUAL_FILE_SYSTEM)){
            Long parentId = Long.parseLong(parentIndex);
            // parentId与ticket不匹配
            Long shareParentId = vFileService.getShareParent(parentId);
            if (!shareParentId.equals(Long.parseLong(shareDTO.getFileIndex())))
                throw new BusinessException(CommonErrorCode.E_600020);
            VFileDTO shareParent = vFileService.query(shareParentId);

            vFileDTO = vFileService.query(shareParent.getStrategyId(), parentId, name);

            // 是目录则把文件数存入size字段
            if (vFileDTO.getType().equals(0))
                vFileDTO.setSize(vFileService.queryCount(Long.parseLong(vFileDTO.getId()) ).longValue());
        } else if (fileSystem.equals(REALY_FILE_SYSTEM)){
            // 对于真实路径，获取的路径应当以分享的路径开头，防止越过权限控制
            if (!parentIndex.startsWith(shareDTO.getFileIndex())){
                throw new BusinessException(CommonErrorCode.E_600020);
            }

            String fileIndex = parentIndex;
            if (!fileIndex.endsWith("/") && !fileIndex.startsWith("\\") ){
                fileIndex += "/";
            }

            vFileDTO = listFiler.queryFile(fileIndex + name,shareDTO.getAccountID()).get(0);
        }

        return vFileDTO;
    }

    @Override
    public List<VFileDTO> queryList(String ticket, String key, String parentIndex, Integer page, Integer limit) {
        ShareDTO shareDTO = shareCheck(ticket, key);
        ListFiler listFiler = fileStorageService.getListFiler(shareDTO.getRoot());

        Integer fileSystem = listFiler.getFileSystem();
        List<VFileDTO> vFileDTOs = null;
        if (fileSystem.equals(VIRTUAL_FILE_SYSTEM)){
            Long parentId = Long.parseLong(parentIndex);
            // vFileId与ticket不匹配
            Long shareParent = vFileService.getShareParent(parentId);
            if (!shareParent.equals(Long.parseLong(shareDTO.getFileIndex())))
                throw new BusinessException(CommonErrorCode.E_600020);

//            vFileDTOs = listFiler.queryDirectory(parentIndex,shareDTO.getAccountID(),page,limit);
        } else if (fileSystem.equals(REALY_FILE_SYSTEM)){
            // 对于真实路径，获取的路径应当以分享的路径开头，防止越过权限控制
            if (!parentIndex.startsWith(shareDTO.getFileIndex())){
                throw new BusinessException(CommonErrorCode.E_600020);
            }
            vFileDTOs = listFiler.queryDirectory(parentIndex,shareDTO.getAccountID(),page,limit);
        }
        vFileDTOs = listFiler.queryDirectory(parentIndex,shareDTO.getAccountID(),page,limit);
        return vFileDTOs;
    }

    @Override
    public String downloadShare(String ticket, String key, String index, Long preference){
        ShareDTO shareDTO = shareCheck(ticket, key);
        ListFiler listFiler = fileStorageService.getListFiler(shareDTO.getRoot());

        String fileIndex = index == null ? shareDTO.getFileIndex() : index;
        Integer fileSystem = listFiler.getFileSystem();
        String res = null;
        res = listFiler.getDownloadUrl(fileIndex, shareDTO.getAccountID(), preference);

        return res;
    }


}
