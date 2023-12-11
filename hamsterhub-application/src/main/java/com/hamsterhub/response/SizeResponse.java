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
    private String total;
    private String usable;
}
