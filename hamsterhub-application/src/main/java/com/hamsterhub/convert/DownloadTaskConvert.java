package com.hamsterhub.convert;

import com.hamsterhub.response.DownloaderOptionResponse;
import com.hamsterhub.response.TaskResponse;
import com.hamsterhub.database.dto.DownloadTaskDTO;
import com.hamsterhub.service.entity.DownloaderOption;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DownloadTaskConvert {
    DownloadTaskConvert INSTANCE = Mappers.getMapper(DownloadTaskConvert.class);
    TaskResponse dto2res(DownloadTaskDTO DTO);
    List<TaskResponse> dto2resBatch(List<DownloadTaskDTO> DTOs);

    List<DownloaderOptionResponse> dto2resBatchForOption(List<DownloaderOption> DTOs);
}
