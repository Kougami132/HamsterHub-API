package com.hamsterhub.service.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.dto.VFileDTO;

import java.util.List;

public interface VFileService {
    VFileDTO create(VFileDTO vFileDTO) throws BusinessException;
    List<Long> delete(Long vFileId) throws BusinessException; // 返回要删除实际文件的ID
    void update(VFileDTO vFileDTO) throws BusinessException;
    VFileDTO query(Long vFileId) throws BusinessException;
    List<Long> deleteBatch(Long accountId, List<Long> vFileIds) throws BusinessException; // 返回要删除实际文件的ID
    List<VFileDTO> queryBatch(Long accountId, String path) throws BusinessException;
    Boolean isExist(Long vFileId) throws BusinessException;
}
