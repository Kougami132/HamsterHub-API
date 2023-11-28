package com.hamsterhub.service.impl;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.device.Storage;
import com.hamsterhub.service.FileService;
import com.hamsterhub.service.StorageService;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.dto.RFileDTO;
import com.hamsterhub.service.dto.StrategyDTO;
import com.hamsterhub.service.service.DeviceService;
import com.hamsterhub.service.service.DeviceStrategyService;
import com.hamsterhub.service.service.RFileService;
import com.hamsterhub.service.service.StrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private StorageService storageService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceStrategyService deviceStrategyService;
    @Autowired
    private RFileService rFileService;

    @Override
    public String getHash(MultipartFile file) throws BusinessException {
        return MD5Util.getMd5(file);
    }

    // 选择设备算法
    private DeviceDTO switchDevice(List<DeviceDTO> deviceDTOs, Integer mode, Long size) {
        DeviceDTO result = null;
        if (mode.equals(0)) { // 优先选择剩余容量大
            Long max = 0L;
            for (DeviceDTO i: deviceDTOs) {
                Storage storage = storageService.getInstance(i);
                if (storage.getUsableSize() > max) {
                    max = storage.getUsableSize();
                    if (max > size)
                        result = i;
                }
            }
        }
        else if (mode.equals(1)) { // 优先选择剩余容量小
            Long min = Long.MAX_VALUE;
            for (DeviceDTO i: deviceDTOs) {
                Storage storage = storageService.getInstance(i);
                if (storage.getUsableSize() < min && storage.getUsableSize() > size) {
                    min = storage.getUsableSize();
                    result = i;
                }
            }
        }
        return result;
    }

    @Override
    public RFileDTO upload(MultipartFile file, StrategyDTO strategyDTO) throws BusinessException {
        String hash = this.getHash(file);
        // 实际文件已存在
        if (rFileService.isExist(hash, strategyDTO.getId()))
            return rFileService.query(hash, strategyDTO.getId());
        List<Long> deviceIds = deviceStrategyService.queryDeviceIds(strategyDTO.getId());
        List<DeviceDTO> deviceDTOs = new ArrayList<>();
        for (Long i: deviceIds)
            deviceDTOs.add(deviceService.query(i));
        // 选择设备算法
        DeviceDTO deviceDTO = switchDevice(deviceDTOs, strategyDTO.getMode(), file.getSize());
        // 设备空间不足
        if (deviceDTO == null)
            throw new BusinessException(CommonErrorCode.E_300007);
        Storage storage = storageService.getInstance(deviceDTO);
        // 上传文件
        String url = storage.upload(file, hash);
        RFileDTO rFileDTO = new RFileDTO(null, hash, hash, url, file.getSize(), deviceDTO.getId());
        rFileDTO = rFileService.create(rFileDTO);
        return rFileDTO;
    }

    @Override
    public String download(RFileDTO rFileDTO) throws BusinessException {
        DeviceDTO deviceDTO = deviceService.query(rFileDTO.getDeviceId());
        Storage storage = storageService.getInstance(deviceDTO);
        return storage.downLoad(rFileDTO.getPath());
    }

    @Override
    public void delete(RFileDTO rFileDTO) throws BusinessException {
        DeviceDTO deviceDTO = deviceService.query(rFileDTO.getDeviceId());
        Storage storage = storageService.getInstance(deviceDTO);
        storage.delete(rFileDTO.getPath());
    }

}
