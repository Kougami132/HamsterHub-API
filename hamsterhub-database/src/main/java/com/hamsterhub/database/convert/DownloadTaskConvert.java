package com.hamsterhub.database.convert;

import com.hamsterhub.database.dto.DownloadTaskDTO;
import com.hamsterhub.database.entity.DownloadTask;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DownloadTaskConvert {
    DownloadTaskConvert INSTANCE = Mappers.getMapper(DownloadTaskConvert.class);

    DownloadTaskDTO entity2dto(DownloadTask entity);
    DownloadTask dto2entity(DownloadTaskDTO dto);

    List<DownloadTaskDTO> entity2dtoBatch(List<DownloadTask> entities);

}
