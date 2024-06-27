package com.hamsterhub.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description="创建修改用户接收数据")
public class UserVO {
    @Schema(description = "主键")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "用户类型")
    private Integer type;

    @Schema(description = "手机号")
    private Long phone;

    @Schema(description = "邮箱")
    private String email;
}
