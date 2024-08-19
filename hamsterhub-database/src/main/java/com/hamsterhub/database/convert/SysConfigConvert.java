package com.hamsterhub.database.convert;

import com.hamsterhub.database.dto.SysConfigDTO;
import com.hamsterhub.database.entity.SysConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SysConfigConvert {
    SysConfigConvert INSTANCE = Mappers.getMapper(SysConfigConvert.class);

    SysConfigDTO entity2dto(SysConfig sysConfig);
    SysConfig dto2entity(SysConfigDTO sysConfigDTO);

    List<SysConfigDTO> entity2dtoBatch(List<SysConfig> sysConfigs);
}
