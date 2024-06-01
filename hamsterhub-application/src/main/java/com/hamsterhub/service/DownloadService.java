package com.hamsterhub.service;

import com.hamsterhub.common.domain.BusinessException;

public interface DownloadService {
    void sendDownloadMsg(String tag, String magnet, Long strategyId, Long parentId, Long accountId) throws BusinessException;
}
