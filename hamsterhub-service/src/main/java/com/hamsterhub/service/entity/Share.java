package com.hamsterhub.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("share")
public class Share {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER)
    private Long id;

    @TableField("TYPE")
    private Integer type;

    @TableField("TICKET")
    private String ticket;

    @TableField("V_FILE_ID")
    private Long vFileId;

    @TableField("KEY")
    private String key;

    @TableField("EXPIRY")
    private LocalDateTime expiry;

    @TableField("ACCOUNT_ID")
    private Long accountID;

}
