package com.hamsterhub.common.domain;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel(value = "RestErrorResponse", description = "错误相应参数包装")
@Data
public class RestErrorResponse {
    private Integer code;
    private String msg;
    public RestErrorResponse(Integer errCode, String errMessage) {
        this.code = errCode;
        this.msg = errMessage;
    }
}
