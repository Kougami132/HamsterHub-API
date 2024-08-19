package com.hamsterhub.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("strategy")
public class Strategy implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ASSIGN_ID)
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

    @TableField("FILE_SYSTEM")
    private Integer fileSystem;

    @TableField("PARAM")
    private String param;
}
