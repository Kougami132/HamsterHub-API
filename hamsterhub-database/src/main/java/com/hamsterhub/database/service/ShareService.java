package com.hamsterhub.database.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.database.dto.ShareDTO;

import java.util.List;

public interface ShareService {
    ShareDTO create(ShareDTO shareDTO) throws BusinessException;
    void delete(Long shareId) throws BusinessException;
    void deleteByVFileId(Long vFileId) throws BusinessException;
    ShareDTO query(Long shareId) throws BusinessException;
    ShareDTO query(String ticket) throws BusinessException;
    List<ShareDTO> queryBatch(Long UserId) throws BusinessException;
    Boolean isExist(Long shareId) throws BusinessException;
    Boolean isExistByVFileId(Long vFileId) throws BusinessException;
    Boolean isExist(String ticket) throws BusinessException;
}
