package com.hamsterhub.convert;

import com.hamsterhub.response.RSSListResponse;
import com.hamsterhub.response.RSSTaskResponse;
import com.hamsterhub.response.VFileResponse;
import com.hamsterhub.service.dto.RSSListDTO;
import com.hamsterhub.service.dto.RSSTaskDTO;
import com.hamsterhub.service.dto.VFileDTO;
import com.hamsterhub.vo.RSSListVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RSSConvert {
    RSSConvert INSTANCE = Mappers.getMapper(RSSConvert.class);

    RSSListDTO vo2dto(RSSListVO vo);

    RSSListResponse dto2res(RSSListDTO dto);
    List<RSSListResponse> dto2resBatch(List<RSSListDTO> dtos);

    List<RSSTaskResponse> dto2resBatchForTask(List<RSSTaskDTO> dtos);
}
