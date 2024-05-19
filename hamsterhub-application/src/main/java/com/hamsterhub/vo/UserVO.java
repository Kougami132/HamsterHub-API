package com.hamsterhub.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel(value="UserVO", description="创建修改用户接收数据")
public class UserVO {
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "用户类型")
    private Integer type;

    @ApiModelProperty(value = "手机号")
    private Long phone;

    @ApiModelProperty(value = "邮箱")
    private String email;
}
