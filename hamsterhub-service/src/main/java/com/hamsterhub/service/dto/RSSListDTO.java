package com.hamsterhub.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description="")
public class RSSListDTO {
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "rss订阅地址")
    private String url;

    @Schema(description = "设置订阅的用户")
    private Long userId;

    @Schema(description = "绑定的策略的root值")
    private String root;

    @Schema(description = "绑定的父目录")
    private String parentIndex;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "当前状态，为1则为启用，为0则为禁用")
    private Integer state;

    @Schema(description = "上一次获取时的文本的hash")
    private String lastHash;

    @Schema(description = "用于替换的域名，如果不为空，则会替换掉种子下载地址中的域名")
    private String replaceHost;
}
