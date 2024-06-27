package com.hamsterhub.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description="设备存储空间返回数据")
public class SizeResponse {
    private String total;
    private String usable;
    public SizeResponse(Long total, Long usable) {
        this.total = total.toString();
        this.usable = usable.toString();
    }
}
