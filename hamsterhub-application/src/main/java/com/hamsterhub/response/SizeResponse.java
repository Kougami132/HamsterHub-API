package com.hamsterhub.response;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(value="DeviceSizeResponse", description="设备存储空间返回数据")
public class SizeResponse {
    private Long total = 0L;
    private Long usable = 0L;
    public void addTotal(Long v) {
        this.total += v;
    }
    public void addUsable(Long v) {
        this.usable += v;
    }
}
