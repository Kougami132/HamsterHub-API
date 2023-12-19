package com.hamsterhub.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value="ShareResponse", description="分享返回数据")
public class ShareResponse {
    private String id;
    private Integer type;
    private String ticket;
    private String vFileId;
    private String key;
    private LocalDateTime expiry;
    private String accountID;
}
