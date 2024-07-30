package com.hamsterhub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description="创建修改存储策略接收数据")
public class StrategyVO {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "策略名称")
    private String name;

    @Schema(description = "策略类型")
    private Integer type;

    @Schema(description = "策略模式")
    private Integer mode;

    @Schema(description = "权限")
    private List<Integer> permissions;

    @Schema(description = "虚拟根目录")
    private String root;

    @Schema(description = "文件组织形式0为虚拟目录 1为真实目录")
    private String fileSystem;

    @Schema(description = "表示存储设备")
    private String param;
}
