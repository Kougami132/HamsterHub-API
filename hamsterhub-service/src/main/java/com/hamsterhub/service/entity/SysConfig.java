package com.hamsterhub.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("sys_config")
public class SysConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "`KEY`", type = IdType.INPUT)
    private String key;

    @TableField("VALUE")
    private String value;

    @TableField("ORDER_ID")
    private Integer orderID;

    @TableField("TYPE")
    private String type;

}