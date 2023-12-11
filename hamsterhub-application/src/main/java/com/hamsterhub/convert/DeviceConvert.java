package com.hamsterhub.convert;

import com.hamsterhub.response.DeviceResponse;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.vo.DeviceVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DeviceConvert {
    DeviceConvert INSTANCE = Mappers.getMapper(DeviceConvert.class);

    DeviceDTO vo2dto(DeviceVO deviceVO);
    DeviceVO dto2vo(DeviceDTO deviceDTO);

    List<DeviceDTO> vo2dtoBatch(List<DeviceVO> devices);

    DeviceDTO res2dto(DeviceResponse deviceResponse);
    DeviceResponse dto2res(DeviceDTO deviceDTO);

    List<DeviceResponse> dto2resBatch(List<DeviceDTO> deviceDTOs);
}
