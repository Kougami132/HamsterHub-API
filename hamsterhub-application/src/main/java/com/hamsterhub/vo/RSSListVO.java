package com.hamsterhub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

@Data
@Schema(description="创建修改RSS列表接收数据")
public class RSSListVO {
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "绑定的策略")
    String root;

    @Schema(description = "绑定的目录")
    String parentIndex;

    @Schema(description = "订阅地址")
    String url;

    @Schema(description = "订阅名称")
    String name;

    @Schema(description = "绑定的下载器")
    Integer downloadId;

    @Schema(description = "用于替换种子下载地址的域名")
    String replaceHost;
}
