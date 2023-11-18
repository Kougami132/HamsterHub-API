package com.hamsterhub.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.service.convert.DeviceStrategyConvert;
import com.hamsterhub.service.dto.DeviceStrategyDTO;
import com.hamsterhub.service.entity.DeviceStrategy;
import com.hamsterhub.service.mapper.DeviceStrategyMapper;
import com.hamsterhub.service.service.DeviceService;
import com.hamsterhub.service.service.DeviceStrategyService;
import com.hamsterhub.service.service.StrategyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class DeviceStrategyServiceImpl implements DeviceStrategyService {

    @Autowired
    private DeviceStrategyMapper deviceStrategyMapper;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private StrategyService strategyService;

    @Override
    public DeviceStrategyDTO create(DeviceStrategyDTO deviceStrategyDTO) throws BusinessException {
        // 传入对象为空
        if (deviceStrategyDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 设备不存在
        if (deviceService.isExist(deviceStrategyDTO.getDeviceId()))
            throw new BusinessException(CommonErrorCode.E_300001);
        // 存储策略不存在
        if (strategyService.isExist(deviceStrategyDTO.getStrategyId()))
            throw new BusinessException(CommonErrorCode.E_400001);
        // 设备已经配置过
        if (this.isDeviceExist(deviceStrategyDTO.getDeviceId()))
            throw new BusinessException(CommonErrorCode.E_300003);

        DeviceStrategy entity = DeviceStrategyConvert.INSTANCE.dto2entity(deviceStrategyDTO);
        entity.setId(null);
        deviceStrategyMapper.insert(entity);
        return DeviceStrategyConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public void delete(Long deviceStrategyId) throws BusinessException {
        // 传入对象为空
        if (deviceStrategyId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 存储设备不存在
        if (this.isExist(deviceStrategyId))
            throw new BusinessException(CommonErrorCode.E_400003);

        deviceStrategyMapper.deleteById(deviceStrategyId);
    }

    @Override
    public void update(DeviceStrategyDTO deviceStrategyDTO) throws BusinessException {
        // 传入对象为空
        if (deviceStrategyDTO == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 设备不存在
        if (deviceService.isExist(deviceStrategyDTO.getDeviceId()))
            throw new BusinessException(CommonErrorCode.E_300001);
        // 存储策略不存在
        if (strategyService.isExist(deviceStrategyDTO.getStrategyId()))
            throw new BusinessException(CommonErrorCode.E_400001);
        // 设备已经配置过
        if (this.isDeviceExist(deviceStrategyDTO.getDeviceId()) && !deviceStrategyMapper.selectOne(new LambdaQueryWrapper<DeviceStrategy>().eq(DeviceStrategy::getDeviceId, deviceStrategyDTO.getDeviceId())).getId().equals(deviceStrategyDTO.getId()))
            throw new BusinessException(CommonErrorCode.E_300003);

        DeviceStrategy entity = DeviceStrategyConvert.INSTANCE.dto2entity(deviceStrategyDTO);
        deviceStrategyMapper.updateById(entity);
    }

    @Override
    public DeviceStrategyDTO query(Long deviceStrategyId) throws BusinessException {
        // 传入对象为空
        if (deviceStrategyId == null)
            throw new BusinessException(CommonErrorCode.E_100001);
        // 存储设备不存在
        if (this.isExist(deviceStrategyId))
            throw new BusinessException(CommonErrorCode.E_400003);

        DeviceStrategy entity = deviceStrategyMapper.selectById(deviceStrategyId);
        return DeviceStrategyConvert.INSTANCE.entity2dto(entity);
    }

    @Override
    public List<DeviceStrategyDTO> queryBatch() throws BusinessException {
        List<DeviceStrategy> entities = deviceStrategyMapper.selectList(null);
        return DeviceStrategyConvert.INSTANCE.entity2dtoBatch(entities);
    }

    @Override
    public Boolean isExist(Long deviceStrategyId) throws BusinessException {
        return deviceStrategyMapper.selectCount(new LambdaQueryWrapper<DeviceStrategy>().eq(DeviceStrategy::getId, deviceStrategyId)) > 0;
    }

    @Override
    public Boolean isDeviceExist(Long deviceId) throws BusinessException {
        return deviceStrategyMapper.selectCount(new LambdaQueryWrapper<DeviceStrategy>().eq(DeviceStrategy::getDeviceId, deviceId)) > 0;
    }

}
