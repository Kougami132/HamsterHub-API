package com.hamsterhub.service;

import com.hamsterhub.common.domain.BusinessException;

public interface ProducerService {
    String sendDownloadMsg(String magnet, Long strategyId, Long parentId, Long accountId) throws BusinessException;

}
