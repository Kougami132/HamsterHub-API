package com.hamsterhub.database.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description="")
public class PushConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "推送类型")
    private String type;

    @Schema(description = "推送参数")
    private String param;

    @Schema(description = "用户ID")
    private Long userId;

    public PushConfigDTO(String type, String param, Long userId) {
        this.type = type;
        this.param = param;
        this.userId = userId;
    }
}
