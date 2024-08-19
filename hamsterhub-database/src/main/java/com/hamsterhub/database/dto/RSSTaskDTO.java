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
public class RSSTaskDTO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "所属用户")
    private Long userId;

    @Schema(description = "关联的rss订阅id")
    private Long rssListId;

    @Schema(description = "文件名")
    private String title;

    @Schema(description = "种子地址")
    private String url;

    @Schema(description = "该任务的发布日期")
    private LocalDateTime pubDate;

    @Schema(description = "文件尺寸")
    private Long size;

    @Schema(description = "状态 0为等待 1为下载 2为异常 3为完成")
    private Integer state;
}
