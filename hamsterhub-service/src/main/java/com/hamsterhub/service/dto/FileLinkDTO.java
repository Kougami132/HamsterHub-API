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
@ApiModel(value="FileLinkDTO", description="")
public class FileLinkDTO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "分享码")
    private String ticket;

    @ApiModelProperty(value = "实际文件ID")
    private Long rFileId;

    @ApiModelProperty(value = "过期时间")
    private LocalDateTime expiry;

    public FileLinkDTO(String ticket, Long rFileId, LocalDateTime expiry) {
        this.ticket = ticket;
        this.rFileId = rFileId;
        this.expiry = expiry;
    }
}
