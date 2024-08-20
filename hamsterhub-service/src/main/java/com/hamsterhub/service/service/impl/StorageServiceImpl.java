package com.hamsterhub.service.service.impl;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.device.Storage;
import com.hamsterhub.service.service.StorageService;
import com.hamsterhub.database.dto.DeviceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class StorageServiceImpl implements StorageService {

    @Autowired
    private List<Storage> storages;

    // 依赖注入完成后执行
    @PostConstruct
    private void init() {
        this.storages.sort(Comparator.comparingInt(Storage::getCode));
    }

    @Override
    public List<String> getTypes() throws BusinessException {
        return storages.stream().map(Storage::getName).collect(toList());
    }

    @Override
    public Storage getInstance(DeviceDTO deviceDTO) throws BusinessException {
        Storage storage = storages.get(deviceDTO.getType());
//        return storage.withDevice(deviceDTO);
        return null;
    }

    @Override
    public Boolean isTypeExist(Integer type) throws BusinessException {
        return type < 0 || type >= this.getTypes().size();
    }

    @Override
    public Boolean verify(DeviceDTO deviceDTO) throws BusinessException {
        Storage storage = storages.get(deviceDTO.getType());
        return storage.verify(deviceDTO);
    }

}
