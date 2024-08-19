package com.hamsterhub.database.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description="")
public class AccountDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "密码上次修改时间")
    private LocalDateTime passModified;

    @Schema(description = "用户类型")
    private Integer type;

    @Schema(description = "手机号")
    private Long phone;

    @Schema(description = "邮箱")
    private String email;

    public AccountDTO(String username, String password, Integer type, Long phone) {
        this.username = username;
        this.password = password;
        this.passModified = LocalDateTime.now();
        this.type = type;
        this.phone = phone;
    }

    public AccountDTO(String username, String password, Integer type, Long phone, String email) {
        this.username = username;
        this.password = password;
        this.passModified = LocalDateTime.now();
        this.type = type;
        this.phone = phone;
        this.email = email;
    }

    public void setPassModified() {
        this.passModified = LocalDateTime.now();
    }

    public boolean isAdmin() {
        return this.type.equals(0);
    }

}