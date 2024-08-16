package com.hamsterhub.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description="RSS返回数据")
public class RSSListResponse {
    private String id;
    private String url;
    private Long userId;
    private String root;
    private String parentIndex;
    private String name;
    private Integer state;
    private String lastHash;
    private String replaceHost;
    private String mirrorHost;
    private Integer downloader;
    private String filter;
}
