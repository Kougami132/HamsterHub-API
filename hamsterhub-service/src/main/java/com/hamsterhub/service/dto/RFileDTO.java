package com.hamsterhub.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description="")
public class RFileDTO {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "文件名")
    private String name;

    @Schema(description = "文件hash值")
    private String hash;

    @Schema(description = "实际存储路径")
    private String path;

    @Schema(description = "文件大小")
    private Long size;

    @Schema(description = "设备ID")
    private Long deviceId;

}
