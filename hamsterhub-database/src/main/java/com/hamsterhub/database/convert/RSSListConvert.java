package com.hamsterhub.database.convert;

import com.hamsterhub.database.dto.RSSListDTO;
import com.hamsterhub.database.entity.RSSList;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RSSListConvert {
    RSSListConvert INSTANCE = Mappers.getMapper(RSSListConvert.class);

    RSSListDTO entity2dto(RSSList entity);
    RSSList dto2entity(RSSListDTO dto);

    List<RSSListDTO> entity2dtoBatch(List<RSSList> entities);
}
