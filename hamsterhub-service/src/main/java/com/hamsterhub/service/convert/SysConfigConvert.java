package com.hamsterhub.service.convert;

import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.dto.SysConfigDTO;
import com.hamsterhub.service.dto.VFileDTO;
import com.hamsterhub.service.entity.Account;
import com.hamsterhub.service.entity.SysConfig;
import com.hamsterhub.service.entity.VFile;
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
