package com.hamsterhub.database.convert;

import com.hamsterhub.database.dto.PushConfigDTO;
import com.hamsterhub.database.entity.PushConfig;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PushConfigConvert {
    PushConfigConvert INSTANCE = Mappers.getMapper(PushConfigConvert.class);

    PushConfigDTO entity2dto(PushConfig pushConfig);
    PushConfig dto2entity(PushConfigDTO pushConfigDTO);

    List<PushConfigDTO> entity2dtoBatch(List<PushConfig> pushConfigs);

    default int booleanToTinyint(Boolean value) {
        return value != null && value ? 1 : 0;
    }
}
