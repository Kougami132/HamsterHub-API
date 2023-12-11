package com.hamsterhub.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="DeviceDTO", description="")
public class DeviceDTO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "设备名称")
    private String name;

    @ApiModelProperty(value = "设备类型")
    private Integer type;

    @ApiModelProperty(value = "参数")
    private String param;

    @ApiModelProperty(value = "已配置")
    private boolean configured;

}
