package com.hamsterhub.service.convert;

import com.hamsterhub.service.dto.DownloadTaskListDTO;
import com.hamsterhub.service.entity.DownloadTaskList;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DownloadTaskListConvert {
    DownloadTaskListConvert INSTANCE = Mappers.getMapper(DownloadTaskListConvert.class);

    DownloadTaskListDTO entity2dto(DownloadTaskList entity);
    DownloadTaskList dto2entity(DownloadTaskListDTO dto);

    List<DownloadTaskListDTO> entity2dtoBatch(List<DownloadTaskList> entities);

}
