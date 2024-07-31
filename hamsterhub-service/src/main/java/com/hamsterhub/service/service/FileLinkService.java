package com.hamsterhub.service.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.dto.FileLinkDTO;

public interface FileLinkService {
    FileLinkDTO create(FileLinkDTO fileLinkDTO) throws BusinessException;
    void update(FileLinkDTO fileLinkDTO) throws BusinessException;
    FileLinkDTO query(Long rFileId) throws BusinessException;
    FileLinkDTO query(String ticket) throws BusinessException;
    Boolean isExist(Long rFileId) throws BusinessException;
    Boolean isExist(String ticket) throws BusinessException;

    void deleteByExpiry();
}
