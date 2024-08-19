package com.hamsterhub.database.convert;

import com.hamsterhub.database.dto.DeviceStrategyDTO;
import com.hamsterhub.database.entity.DeviceStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DeviceStrategyConvert {
    DeviceStrategyConvert INSTANCE = Mappers.getMapper(DeviceStrategyConvert.class);

    DeviceStrategyDTO entity2dto(DeviceStrategy deviceStrategy);
    DeviceStrategy dto2entity(DeviceStrategyDTO deviceStrategyDTO);

    List<DeviceStrategyDTO> entity2dtoBatch(List<DeviceStrategy> deviceStrategies);
}
