package com.hamsterhub.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@ApiModel(value="AccountDTO", description="")
public class AccountDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String password;

    @ApiModelProperty(value = "密码上次修改时间")
    private LocalDateTime passModified;

    @ApiModelProperty(value = "用户类型")
    private Integer type;

    @ApiModelProperty(value = "手机号")
    private Integer phone;

    @ApiModelProperty(value = "邮箱")
    private String email;

    public AccountDTO(String username, String password, Integer type) {
        this.username = username;
        this.password = password;
        this.passModified = LocalDateTime.now();
        this.type = type;
    }

    public boolean isAdmin() {
        return this.type.equals(0);
    }

}