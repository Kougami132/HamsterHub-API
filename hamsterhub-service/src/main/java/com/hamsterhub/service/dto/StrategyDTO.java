package com.hamsterhub.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="StrategyDTO", description="")
public class StrategyDTO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "存储策略名称")
    private String name;

    @ApiModelProperty(value = "存储类型")
    private Integer type;

    @ApiModelProperty(value = "存储优先级模式")
    private Integer mode;

    @ApiModelProperty(value = "权限")
    private Integer permission;

    @ApiModelProperty(value = "虚拟根目录")
    private String root;

}
