package com.hamsterhub.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("share")
public class Share implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("TYPE")
    private Integer type;

    @TableField("TICKET")
    private String ticket;

    @TableField("ROOT")
    private String root;

    @TableField("FILE_INDEX")
    private String fileIndex;

    @TableField("`KEY`")
    private String key;

    @TableField("`EXPIRY`")
    private LocalDateTime expiry;

    @TableField("USER_ID")
    private Long userId;

    @TableField("NAME")
    private String name;

}
