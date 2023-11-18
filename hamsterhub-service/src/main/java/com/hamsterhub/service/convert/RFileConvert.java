package com.hamsterhub.service.convert;

import com.hamsterhub.service.dto.RFileDTO;
import com.hamsterhub.service.entity.RFile;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RFileConvert {
    RFileConvert INSTANCE = Mappers.getMapper(RFileConvert.class);

    RFileDTO entity2dto(RFile rFile);
    RFile dto2entity(RFileDTO rFileDTO);
}
