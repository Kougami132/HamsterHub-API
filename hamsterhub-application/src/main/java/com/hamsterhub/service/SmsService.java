package com.hamsterhub.service;

import com.hamsterhub.common.domain.BusinessException;

public interface SmsService {
    void sendAliCode(Long phone, String code) throws BusinessException;
    void sendTencentCode(Long phone, String code) throws BusinessException;
}
