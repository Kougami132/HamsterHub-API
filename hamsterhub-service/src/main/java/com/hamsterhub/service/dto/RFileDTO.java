package com.hamsterhub.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(value="RFileDTO", description="")
public class RFileDTO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "文件名")
    private String name;

    @ApiModelProperty(value = "文件hash值")
    private String hash;

    @ApiModelProperty(value = "实际存储路径")
    private String path;

    @ApiModelProperty(value = "文件大小")
    private Long size;

    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

}
