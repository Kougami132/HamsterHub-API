package com.hamsterhub.convert;

import com.hamsterhub.database.dto.DownloadTaskDTO;
import com.hamsterhub.database.dto.PushConfigDTO;
import com.hamsterhub.response.DownloaderOptionResponse;
import com.hamsterhub.response.PushConfigResponse;
import com.hamsterhub.response.TaskResponse;
import com.hamsterhub.service.entity.DownloaderOption;
import com.hamsterhub.vo.PushConfigVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PushConfigConvert {
    PushConfigConvert INSTANCE = Mappers.getMapper(PushConfigConvert.class);

    PushConfigDTO vo2dto(PushConfigVO vo);
    PushConfigVO dto2vo(PushConfigDTO DTO);

    PushConfigDTO res2dto(PushConfigResponse res);
    PushConfigResponse dto2res(PushConfigDTO DTO);
}
