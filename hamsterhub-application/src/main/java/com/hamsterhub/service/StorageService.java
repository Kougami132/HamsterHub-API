package com.hamsterhub.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.device.Storage;
import com.hamsterhub.service.dto.DeviceDTO;

import java.util.List;

public interface StorageService {
    List<String> getTypes() throws BusinessException;
    Storage getInstance(DeviceDTO deviceDTO) throws BusinessException;
}
