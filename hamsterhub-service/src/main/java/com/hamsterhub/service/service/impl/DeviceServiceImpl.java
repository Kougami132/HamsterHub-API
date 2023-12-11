package com.hamsterhub.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.service.convert.DeviceConvert;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.entity.Device;
import com.hamsterhub.service.mapper.DeviceMapper;
import com.hamsterhub.service.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceMapper deviceMapper;

    @Override
    public DeviceDTO create(DeviceDTO deviceDTO) throws BusinessException {
        // 传入对象为空
        if (deviceDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);

        Device entity = DeviceConvert.INSTANCE.dto2entity(deviceDTO);
        entity.setId(null);
        deviceMapper.insert(entity);
        return DeviceConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void delete(Long deviceId) throws BusinessException {
        // 传入对象为空
        if (deviceId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 存储设备不存在
        if (!this.isExist(deviceId))
            throw new BusinessException(CommonErrorCode.E_300001);

        deviceMapper.deleteById(deviceId);
    }

    @Override
    public void update(DeviceDTO deviceDTO) throws BusinessException {
        // 传入对象为空
        if (deviceDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 设备名已存在
        Wrapper exist = new LambdaQueryWrapper<Device>().eq(Device::getName, deviceDTO.getName());
        if (deviceMapper.selectCount(exist) > 0 && !deviceMapper.selectOne(exist).getId().equals(deviceDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_300002);

        Device entity = DeviceConvert.INSTANCE.dto2entity(deviceDTO);
        deviceMapper.updateById(entity);
    }

    @Override
    public DeviceDTO query(Long deviceId) throws BusinessException {
        // 传入对象为空
        if (deviceId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 存储设备不存在
        if (!this.isExist(deviceId))
            throw new BusinessException(CommonErrorCode.E_300001);

        Device entity = deviceMapper.selectById(deviceId);
        return DeviceConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public List<DeviceDTO> queryBatch() throws BusinessException {
        List<Device> entities = deviceMapper.selectList(null);
        return DeviceConvert.INSTANCE.entity2dtoBatch(entities);
    }

    @Override
    public Boolean isExist(Long deviceId) throws BusinessException {
        return deviceMapper.selectCount(new LambdaQueryWrapper<Device>().eq(Device::getId, deviceId)) > 0;
    }

    @Override
    public void configured(Long deviceId, boolean conf) throws BusinessException {
        // 存储设备不存在
        if (!this.isExist(deviceId))
            throw new BusinessException(CommonErrorCode.E_300001);

        Device entity = deviceMapper.selectById(deviceId);
        entity.setConfigured(conf);
        deviceMapper.updateById(entity);
    }
}
