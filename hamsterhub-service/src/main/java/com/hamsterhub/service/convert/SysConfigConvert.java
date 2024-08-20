package com.hamsterhub.service.convert;

import com.hamsterhub.service.entity.SysConfigResponse;
import com.hamsterhub.database.dto.SysConfigDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysConfigConvert {
    SysConfigConvert INSTANCE = Mappers.getMapper(SysConfigConvert.class);

    List<SysConfigResponse> dto2resBatch(List<SysConfigDTO> sysConfigDTOs);
    SysConfigResponse dto2res(SysConfigDTO sysConfigDTO);
}
