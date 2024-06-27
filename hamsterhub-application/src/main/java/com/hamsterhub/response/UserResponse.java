package com.hamsterhub.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description="用户管理返回数据")
public class UserResponse {
    private String id;
    private String username;
    private String password;
    private Integer type;
    private String phone;
    private String email;
}
