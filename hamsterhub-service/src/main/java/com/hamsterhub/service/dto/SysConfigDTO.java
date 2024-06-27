package com.hamsterhub.service.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@NoArgsConstructor
@Data
@Schema(description="")
public class SysConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String key;

    @Schema(description = "值")
    private String value;

    @Schema(description = "排序id")
    private Integer orderID ;

    @Schema(description = "数据类型，决定前端的输入和显示形式")
    private String type;

    @Schema(description = "是否隐藏，为真之后对应配置不会发给前端")
    private Boolean hide;

    public SysConfigDTO(String key, String value, Integer orderID, String type, Boolean hide) {
        this.key = key;
        this.value = value;
        this.orderID = orderID;
        this.type = type;
        this.hide = hide;
    }

    public SysConfigDTO(String key, String value) {
        this.key = key;
        this.value = value;
        this.orderID = null;
        this.type = null;
        this.hide = null;
    }

}