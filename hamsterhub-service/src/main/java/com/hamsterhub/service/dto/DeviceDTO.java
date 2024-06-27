package com.hamsterhub.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description="")
public class DeviceDTO {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "设备类型")
    private Integer type;

    @Schema(description = "参数")
    private String param;

    @Schema(description = "已配置")
    private boolean configured;

    @Schema(description = "已连接")
    private boolean connected;

}
