package com.hamsterhub.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description="推送配置返回数据")
public class PushConfigResponse {
    private String enable;
    private String type;
    private String param;
}
