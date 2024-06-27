package com.hamsterhub.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@Schema(description="")
public class DeviceStrategyDTO {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "设备ID")
    private Long deviceId;

    @Schema(description = "存储策略ID")
    private Long strategyId;

    public DeviceStrategyDTO(Long deviceId, Long strategyId) {
        this.deviceId = deviceId;
        this.strategyId = strategyId;
    }

}
