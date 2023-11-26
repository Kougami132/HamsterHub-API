package com.hamsterhub.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(value="ShareDTO", description="")
public class ShareDTO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "分享类型")
    private Integer type;

    @ApiModelProperty(value = "分享码")
    private String ticket;

    @ApiModelProperty(value = "分享码")
    private Long vFileId;

    @ApiModelProperty(value = "提取码")
    private String key;

    @ApiModelProperty(value = "过期时间")
    private LocalDateTime expiry;

    @ApiModelProperty(value = "文件所有人ID")
    private Long accountID;

}
