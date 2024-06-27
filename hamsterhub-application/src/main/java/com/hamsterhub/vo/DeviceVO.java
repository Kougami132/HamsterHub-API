package com.hamsterhub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description="创建修改设备接收数据")
public class DeviceVO {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "设备名称")
    private String name;

    @Schema(description = "设备类型")
    private Integer type;

    @Schema(description = "设备参数")
    private String param = "";
}
