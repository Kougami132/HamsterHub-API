package com.hamsterhub.service.convert;

import com.hamsterhub.service.dto.RSSTaskDTO;
import com.hamsterhub.service.entity.RSSTask;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RSSTaskConvert {
    RSSTaskConvert INSTANCE = Mappers.getMapper(RSSTaskConvert.class);

    RSSTaskDTO entity2dto(RSSTask entity);
    RSSTask dto2entity(RSSTaskDTO dto);

    List<RSSTaskDTO> entity2dtoBatch(List<RSSTask> entities);
}
