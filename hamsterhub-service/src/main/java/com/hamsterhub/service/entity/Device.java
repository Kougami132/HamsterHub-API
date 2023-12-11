package com.hamsterhub.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("device")
public class Device implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER)
    private Long id;

    @TableField("NAME")
    private String name;

    @TableField("TYPE")
    private Integer type;

    @TableField("PARAM")
    private String param;

    @TableField("CONFIGURED")
    private boolean configured = false;
}
