package com.hamsterhub.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value="UserResponse", description="用户管理返回数据")
public class UserResponse {
    private String id;
    private String username;
    private String password;
    private Integer type;
    private String phone;
    private String email;
}
