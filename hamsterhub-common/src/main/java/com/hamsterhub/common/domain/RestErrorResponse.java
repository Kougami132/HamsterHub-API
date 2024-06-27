package com.hamsterhub.common.domain;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema( description = "错误相应参数包装" )
@Data
public class RestErrorResponse {
    @Schema( description = "错误码")
    private Integer code;

    @Schema( description = "希望前端显示的消息")
    private String msg;
    public RestErrorResponse(Integer errCode, String errMessage) {
        this.code = errCode;
        this.msg = errMessage;
    }
}
