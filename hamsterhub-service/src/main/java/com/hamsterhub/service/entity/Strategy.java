package com.hamsterhub.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("strategy")
public class Strategy {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER)
    private Long id;

    @TableField("NAME")
    private String name;

    @TableField("TYPE")
    private Integer type;

    @TableField("MODE")
    private Integer mode;

    @TableField("PERMISSION")
    private Integer permission;

    @TableField("ROOT")
    private String root;
}
