package com.hamsterhub.database.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.database.dto.VFileDTO;

import java.util.List;

public interface VFileService {
    VFileDTO create(VFileDTO vFileDTO) throws BusinessException;

    VFileDTO createOverlay(VFileDTO vFileDTO) throws BusinessException;

    VFileDTO createDir(VFileDTO vFileDTO) throws BusinessException;
    List<String> delete(Long vFileId) throws BusinessException; // 返回要删除实际文件的ID
    void update(VFileDTO vFileDTO) throws BusinessException;
    VFileDTO query(Long vFileId) throws BusinessException;

    VFileDTO query(Long vFileId, Long accountId) throws BusinessException;

    VFileDTO query(Long strategyId, Long parentId, String name) throws BusinessException;
    List<VFileDTO> query(Long accountId, Long strategyId, Long parentId, String name) throws BusinessException;
    List<VFileDTO> query(Long accountId, String root, Long parentId, String name) throws BusinessException;
    List<VFileDTO> queryBatch(Long parentId) throws BusinessException; // 分享用，id不为0
    List<VFileDTO> queryBatch(Long parentId, Integer page, Integer limit) throws BusinessException; // 分享用，id不为0
    List<VFileDTO> queryBatch(Long accountId, Long strategyId, Long parentId) throws BusinessException;
    List<VFileDTO> queryBatch(Long accountId, String root, Long parentId) throws BusinessException;
    List<VFileDTO> queryBatch(Long accountId, String root, Long parentId, Integer page, Integer limit) throws BusinessException;
    Integer queryCount(Long vFileId) throws BusinessException;
    Boolean isExist(Long vFileId) throws BusinessException;
    Boolean isExist(Long accountId, Long strategyId, Long parentId, String name) throws BusinessException;
    Boolean isExist(Long accountId, String root, Long parentId, String name) throws BusinessException;
    void rename(Long vFileId, String newName) throws BusinessException;
    Long getShareParent(Long vFileId) throws BusinessException;
}
