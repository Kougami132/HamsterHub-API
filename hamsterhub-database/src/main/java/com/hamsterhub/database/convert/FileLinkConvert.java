package com.hamsterhub.database.convert;

import com.hamsterhub.database.dto.FileLinkDTO;
import com.hamsterhub.database.entity.FileLink;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FileLinkConvert {
    FileLinkConvert INSTANCE = Mappers.getMapper(FileLinkConvert.class);

    FileLinkDTO entity2dto(FileLink fileLink);
    FileLink dto2entity(FileLinkDTO fileLinkDTO);
}
