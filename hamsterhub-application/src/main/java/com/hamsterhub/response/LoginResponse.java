package com.hamsterhub.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Schema(description="登录返回数据")
public class LoginResponse {
    private String id;
    private String type;
    private String username;
    private String token;
}
