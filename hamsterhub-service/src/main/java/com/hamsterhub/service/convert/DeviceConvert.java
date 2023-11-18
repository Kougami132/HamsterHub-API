package com.hamsterhub.service.convert;

import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.entity.Device;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DeviceConvert {
    DeviceConvert INSTANCE = Mappers.getMapper(DeviceConvert.class);

    DeviceDTO entity2dto(Device device);
    Device dto2entity(DeviceDTO deviceDTO);

    List<DeviceDTO> entity2dtoBatch(List<Device> devices);
}
