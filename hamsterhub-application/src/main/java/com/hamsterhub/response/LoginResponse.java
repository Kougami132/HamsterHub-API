package com.hamsterhub.response;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@ApiModel(value="LoginResponse", description="登录返回数据")
public class LoginResponse {
    private String id;
    private String username;
    private String token;
    private Integer type;
}
