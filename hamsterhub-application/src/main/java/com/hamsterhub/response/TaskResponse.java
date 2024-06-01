package com.hamsterhub.response;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="TaskResponse", description="任务返回数据")
public class TaskResponse {
    private String tag;
    private String state;
    private String name;
    private Long completed;
    private Long total;
}
