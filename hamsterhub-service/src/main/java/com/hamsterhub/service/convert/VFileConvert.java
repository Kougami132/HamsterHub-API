package com.hamsterhub.service.convert;

import com.hamsterhub.service.dto.VFileDTO;
import com.hamsterhub.service.entity.VFile;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper
public interface VFileConvert {
    VFileConvert INSTANCE = Mappers.getMapper(VFileConvert.class);

    VFileDTO entity2dto(VFile vFile);
    VFile dto2entity(VFileDTO vFileDTO);

    List<VFileDTO> entity2dtoBatch(List<VFile> vFiles);

    // 将 LocalDateTime 转换为以毫秒为单位的 Unix 时间戳
    default Long localDateTimeToLong(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toInstant(ZoneOffset.UTC).toEpochMilli() : null;
    }

    // 将以毫秒为单位的 Unix 时间戳转换为 LocalDateTime
    default LocalDateTime longToLocalDateTime(Long timestamp) {
        return timestamp != null ? LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC) : null;
    }
}
