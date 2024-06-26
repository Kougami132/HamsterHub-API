package com.hamsterhub.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description="文件返回数据")
public class VFileResponse {
    private String id;
    private Integer type;
    private String name;
    private String parentId;
//    private String rFileId;
    private Integer version;
    private LocalDateTime created;
    private LocalDateTime modified;
//    private String accountID;
    private String size;
//    private String strategyId;
    private Integer shareType;
}
