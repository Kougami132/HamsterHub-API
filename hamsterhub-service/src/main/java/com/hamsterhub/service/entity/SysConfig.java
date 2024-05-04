package com.hamsterhub.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_config")
public class SysConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "`key`", type = IdType.INPUT)
    private String key;

    @TableField("value")
    private String value;

    @TableField("order_id")
    private Integer orderID ;

    @TableField("type")
    private String type;

    @TableField("hide")
    private Boolean hide;

}