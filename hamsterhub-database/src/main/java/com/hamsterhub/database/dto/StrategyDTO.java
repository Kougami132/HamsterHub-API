package com.hamsterhub.database.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description="")
public class StrategyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "存储策略名称")
    private String name;

    @Schema(description = "存储类型")
    private Integer type;

    @Schema(description = "存储优先级模式")
    private Integer mode;

    @Schema(description = "权限")
    private Integer permission;

    @Schema(description = "虚拟根目录")
    private String root;

    @Schema(description = "文件组织形式0为虚拟目录 1为真实目录")
    private String fileSystem;

    @Schema(description = "表示存储设备")
    private String param;

}
