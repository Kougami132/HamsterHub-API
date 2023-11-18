package com.hamsterhub.service.convert;

import com.hamsterhub.service.dto.VFileDTO;
import com.hamsterhub.service.entity.VFile;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface VFileConvert {
    VFileConvert INSTANCE = Mappers.getMapper(VFileConvert.class);

    VFileDTO entity2dto(VFile vFile);
    VFile dto2entity(VFileDTO vFileDTO);

    List<VFileDTO> entity2dtoBatch(List<VFile> vFiles);
}
