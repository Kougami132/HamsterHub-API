package com.hamsterhub.database.convert;

import com.hamsterhub.database.dto.DeviceDTO;
import com.hamsterhub.database.entity.Device;
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
