package com.hamsterhub.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description="存储设备返回数据")
public class DeviceResponse {
    private String id;
    private String name;
    private Integer type;
    private String param;
    private Boolean configured;
    private Boolean connected;
    private String StrategyId = "0";
    private SizeResponse size;
}
