package com.hamsterhub.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("push_config")
public class PushConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("ENABLE")
    private Boolean enable;

    @TableField("TYPE")
    private String type;

    @TableField("PARAM")
    private String param;

    @TableField("USER_ID")
    private Long userId;

}
