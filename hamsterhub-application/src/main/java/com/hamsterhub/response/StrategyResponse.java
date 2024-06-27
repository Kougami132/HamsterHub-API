package com.hamsterhub.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description="存储策略返回数据")
public class StrategyResponse {
    private String id;
    private String name;
    private Integer type;
    private Integer mode;
    private List<Integer> permissions;
    private String root;
    private List<String> deviceIds;
    private SizeResponse size;
}
