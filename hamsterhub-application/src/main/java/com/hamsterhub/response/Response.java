package com.hamsterhub.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Schema(description = "请求返回数据")
@Data
public class Response {
    private Integer code;
    private String msg;
    private Object data;
    public static Response success() {
        return new Response(0, "", null);
    }
    public Response code(Integer code) {
        this.code = code;
        return this;
    }
    public Response msg(String msg) {
        this.msg = msg;
        return this;
    }
    public Response data(Object data) {
        this.data = data;
        return this;
    }
}
