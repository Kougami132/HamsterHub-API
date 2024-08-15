package com.hamsterhub.convert;

import com.hamsterhub.response.DownloaderOptionResponse;
import com.hamsterhub.response.TaskResponse;
import com.hamsterhub.response.UserResponse;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.dto.DownloadTaskListDTO;
import com.hamsterhub.service.dto.DownloaderOptionDTO;
import com.hamsterhub.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TaskConvert {
    TaskConvert INSTANCE = Mappers.getMapper(TaskConvert.class);
    TaskResponse dto2res(DownloadTaskListDTO DTO);
    List<TaskResponse> dto2resBatch(List<DownloadTaskListDTO> DTOs);

    List<DownloaderOptionResponse> dto2resBatchForOption(List<DownloaderOptionDTO> DTOs);
}
