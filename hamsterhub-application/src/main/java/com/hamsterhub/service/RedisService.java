package com.hamsterhub.service;

import com.hamsterhub.common.domain.BusinessException;

public interface RedisService {
    Long getFileId(String root, Long accountId, String path) throws BusinessException;
    void setFileId(String root, Long accountId, String path, Long fileId) throws BusinessException;
    void delFileId(String root, Long accountId, String path) throws BusinessException;
    boolean isPathExist(String root, Long accountId, String path) throws BusinessException;
}
