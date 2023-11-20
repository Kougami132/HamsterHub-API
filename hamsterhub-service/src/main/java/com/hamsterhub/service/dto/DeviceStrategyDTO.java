package com.hamsterhub.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@ApiModel(value="DeviceStrategyDTO", description="")
public class DeviceStrategyDTO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    @ApiModelProperty(value = "存储策略ID")
    private Long strategyId;

    public DeviceStrategyDTO(Long deviceId, Long strategyId) {
        this.deviceId = deviceId;
        this.strategyId = strategyId;
    }

}
