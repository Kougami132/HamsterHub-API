package com.hamsterhub.database.convert;

import com.hamsterhub.database.dto.ShareDTO;
import com.hamsterhub.database.entity.Share;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ShareConvert {
    ShareConvert INSTANCE = Mappers.getMapper(ShareConvert.class);

    ShareDTO entity2dto(Share share);
    Share dto2entity(ShareDTO shareDTO);

    List<ShareDTO> entity2dtoBatch(List<Share> shares);
}
