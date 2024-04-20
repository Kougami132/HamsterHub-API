package com.hamsterhub.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("account")
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER)
    private Long id;

    @TableField("USERNAME")
    private String username;

    @TableField("PASSWORD")
    private String password;

    @TableField("PASS_MODIFIED")
    private LocalDateTime passModified;

    @TableField("TYPE")
    private Integer type;

    @TableField("PHONE")
    private Integer phone;

    @TableField("EMAIL")
    private String email;

}