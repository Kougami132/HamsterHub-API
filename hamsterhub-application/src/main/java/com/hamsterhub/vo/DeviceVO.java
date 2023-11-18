package com.hamsterhub.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="DeviceVO", description="创建修改设备接收数据")
public class DeviceVO {
    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "设备名称")
    private String name;

    @ApiModelProperty(value = "设备类型")
    private Integer type;

    @ApiModelProperty(value = "设备参数")
    private String param;
}
