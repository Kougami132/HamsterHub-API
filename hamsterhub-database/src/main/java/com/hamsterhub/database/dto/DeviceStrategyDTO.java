package com.hamsterhub.database.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Data
@Schema(description="")
public class DeviceStrategyDTO implements Serializable {
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
