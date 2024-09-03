package com.hamsterhub.database.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.database.dto.PushConfigDTO;

import java.util.List;

public interface PushConfigService {
    PushConfigDTO create(PushConfigDTO pushConfigDTO) throws BusinessException;
    void delete(Long userId) throws BusinessException;
    PushConfigDTO query(Long userId) throws BusinessException;
    void update(PushConfigDTO pushConfigDTO) throws BusinessException;
    Boolean isExist(Long userId) throws BusinessException;
}
