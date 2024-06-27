package com.hamsterhub.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description="")
public class ShareDTO {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "分享类型")
    private Integer type;

    @Schema(description = "分享码")
    private String ticket;

    @Schema(description = "分享码")
    private Long vFileId;

    @Schema(description = "提取码")
    private String key;

    @Schema(description = "过期时间")
    private LocalDateTime expiry;

    @Schema(description = "文件所有人ID")
    private Long accountID;

    @Schema(description = "分享的名称")
    private String name;

}
