package com.hamsterhub.service.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value="VFileDTO", description="")
public class VFileDTO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "文件类型")
    private Integer type;

    @ApiModelProperty(value = "文件名")
    private String name;

    @ApiModelProperty(value = "虚拟文件路径")
    private String path;

    @ApiModelProperty(value = "实际文件ID")
    private Long rFileId;

    @ApiModelProperty(value = "文件版本")
    private Integer version;

    @ApiModelProperty(value = "文件修改时间")
    private LocalDateTime timestamp;

    @ApiModelProperty(value = "文件所有人ID")
    private Long accountID;

    @ApiModelProperty(value = "文件权限")
    private Integer permission;

    @ApiModelProperty(value = "存储策略ID")
    private Long strategyId;

}
