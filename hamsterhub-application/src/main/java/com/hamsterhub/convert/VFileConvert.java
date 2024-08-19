package com.hamsterhub.convert;

import com.hamsterhub.response.VFileResponse;
import com.hamsterhub.database.dto.VFileDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface VFileConvert {
    VFileConvert INSTANCE = Mappers.getMapper(VFileConvert.class);

    VFileResponse dto2res(VFileDTO vFileDTO);
    List<VFileResponse> dto2resBatch(List<VFileDTO> vFileDTOs);
}
