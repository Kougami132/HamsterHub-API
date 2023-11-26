package com.hamsterhub.service.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.dto.ShareDTO;

import java.util.List;

public interface ShareService {
    ShareDTO create(ShareDTO shareDTO) throws BusinessException;
    void delete(Long shareId) throws BusinessException;
    void deleteByVFileId(Long vFileId) throws BusinessException;
    ShareDTO query(Long shareId) throws BusinessException;
    ShareDTO query(String ticket) throws BusinessException;
    List<ShareDTO> queryBatch(Long AccountId) throws BusinessException;
    Boolean isExist(Long shareId) throws BusinessException;
    Boolean isExistByVFileId(Long vFileId) throws BusinessException;
    Boolean isExist(String ticket) throws BusinessException;
}
