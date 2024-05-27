package com.hamsterhub.service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DownloadTask {
    private String tag;
    private String magnet;
    private Long strategyId;
    private Long parentId;
    private Long accountId;
}
