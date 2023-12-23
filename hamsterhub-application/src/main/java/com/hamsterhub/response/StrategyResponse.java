package com.hamsterhub.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value="StrategyResponse", description="存储策略返回数据")
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
