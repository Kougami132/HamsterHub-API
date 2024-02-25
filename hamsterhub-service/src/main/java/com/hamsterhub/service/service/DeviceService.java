package com.hamsterhub.service.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.dto.DeviceDTO;

import java.util.List;

public interface DeviceService {
    DeviceDTO create(DeviceDTO deviceDTO) throws BusinessException;
    void delete(Long deviceId) throws BusinessException;
    void update(DeviceDTO deviceDTO) throws BusinessException;
    DeviceDTO query(Long deviceId) throws BusinessException;
    List<DeviceDTO> queryBatch() throws BusinessException;
    Boolean isExist(Long deviceId) throws BusinessException;
    Boolean isLocalExist(String path) throws BusinessException;
    void configured(Long deviceId, boolean conf) throws BusinessException;
}
