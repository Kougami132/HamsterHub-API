package com.hamsterhub.response;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value="SysConfigResponse", description="系统设置返回数据")
public class SysConfigResponse {
    private String key;
    private String value;
    private Integer orderID ;
    private String type;

    public SysConfigResponse(String key, String value) {
        this.key = key;
        this.value = value;
        this.orderID = null;
        this.type = null;
    }
}
