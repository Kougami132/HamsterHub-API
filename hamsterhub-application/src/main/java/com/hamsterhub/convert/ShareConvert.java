package com.hamsterhub.convert;

import com.hamsterhub.response.ShareResponse;
import com.hamsterhub.database.dto.ShareDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ShareConvert {
    ShareConvert INSTANCE = Mappers.getMapper(ShareConvert.class);

    ShareResponse dto2res(ShareDTO shareDTO);
    List<ShareResponse> dto2resBatch(List<ShareDTO> shareDTOs);
}
