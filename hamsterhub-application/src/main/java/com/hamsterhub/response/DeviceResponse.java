package com.hamsterhub.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value="DeviceResponse", description="存储设备返回数据")
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
