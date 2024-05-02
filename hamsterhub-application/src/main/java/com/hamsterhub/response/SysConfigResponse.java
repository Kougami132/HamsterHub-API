package com.hamsterhub.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value="SysConfigResponse", description="系统设置返回数据")
public class SysConfigResponse {
    private String key;
    private String value;
    private Integer orderID ;
    private String type;
}
