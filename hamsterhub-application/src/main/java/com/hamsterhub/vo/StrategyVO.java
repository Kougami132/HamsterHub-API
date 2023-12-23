package com.hamsterhub.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value="StrategyVO", description="创建修改存储策略接收数据")
public class StrategyVO {
    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "策略名称")
    private String name;

    @ApiModelProperty(value = "策略类型")
    private Integer type;

    @ApiModelProperty(value = "策略模式")
    private Integer mode;

    @ApiModelProperty(value = "权限")
    private List<Integer> permissions;

    @ApiModelProperty(value = "虚拟根目录")
    private String root;

    @ApiModelProperty(value = "策略绑定的设备ID")
    private List<Long> deviceIds;
}
