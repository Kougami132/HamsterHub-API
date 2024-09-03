package com.hamsterhub.service.service;

import com.hamsterhub.common.domain.BusinessException;

public interface PushService {
    void push(String params, String message) throws BusinessException;
}
