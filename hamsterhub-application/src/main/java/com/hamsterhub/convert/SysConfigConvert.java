package com.hamsterhub.convert;

import com.hamsterhub.response.SysConfigResponse;
import com.hamsterhub.response.VFileResponse;
import com.hamsterhub.service.dto.SysConfigDTO;
import com.hamsterhub.service.dto.VFileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysConfigConvert {
    SysConfigConvert INSTANCE = Mappers.getMapper(SysConfigConvert.class);

    List<SysConfigResponse> dto2resBatch(List<SysConfigDTO> sysConfigDTOs);
}
