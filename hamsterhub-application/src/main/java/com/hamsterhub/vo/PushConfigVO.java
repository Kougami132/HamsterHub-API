package com.hamsterhub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description="设置推送配置接收数据")
public class PushConfigVO {
    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "推送类型")
    private String type;

    @Schema(description = "推送参数")
    private String param;
}
