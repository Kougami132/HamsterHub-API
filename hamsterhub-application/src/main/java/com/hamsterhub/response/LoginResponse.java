package com.hamsterhub.response;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@ApiModel(value="LoginResponse", description="登录返回数据")
public class LoginResponse {
    public String username;
    public String token;
}
