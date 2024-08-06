package com.hamsterhub.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description="RSS Task返回数据")
public class RSSTaskResponse {
    private String id;
    private String rssListId;
    private String title;
    private String url;
    private LocalDateTime pubDate;
    private Long size;
    private Integer state;
}
