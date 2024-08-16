package com.hamsterhub.service;

import com.hamsterhub.common.domain.BusinessException;

public interface PushService {
    void pushGoCq(Boolean isGroup, Long targetId, String message) throws BusinessException;
}
