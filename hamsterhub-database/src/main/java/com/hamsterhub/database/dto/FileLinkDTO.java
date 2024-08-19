package com.hamsterhub.database.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description="")
public class FileLinkDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "分享码")
    private String ticket;

    @Schema(description = "实际文件ID")
    private Long rFileId;

    @Schema(description = "过期时间")
    private LocalDateTime expiry;

    @Schema(description = "文件位置")
    private String path;

    public FileLinkDTO(String ticket, Long rFileId, LocalDateTime expiry) {
        this.ticket = ticket;
        this.rFileId = rFileId;
        this.expiry = expiry;
        this.path = null;
    }

    public FileLinkDTO(String ticket, Long rFileId, LocalDateTime expiry, String path) {
        this.ticket = ticket;
        this.rFileId = rFileId;
        this.expiry = expiry;
        this.path = path;
    }

}
