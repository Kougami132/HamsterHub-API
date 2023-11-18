package com.hamsterhub.service.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.dto.DeviceStrategyDTO;

import java.util.List;

public interface DeviceStrategyService {
    DeviceStrategyDTO create(DeviceStrategyDTO deviceStrategyDTO) throws BusinessException;
    void delete(Long deviceStrategyId) throws BusinessException;
    void update(DeviceStrategyDTO deviceStrategyDTO) throws BusinessException;
    DeviceStrategyDTO query(Long deviceStrategyId) throws BusinessException;
    List<DeviceStrategyDTO> queryBatch() throws BusinessException;
    Boolean isExist(Long deviceStrategyId) throws BusinessException;
    Boolean isDeviceExist(Long deviceId) throws BusinessException;
}
