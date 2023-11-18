package com.hamsterhub.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("v_file")
public class VFile {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER)
    private Long id;

    @TableField("TYPE")
    private Integer type;

    @TableField("NAME")
    private String name;

    @TableField("PATH")
    private String path;

    @TableField("R_FILE_ID")
    private Long rFileId;

    @TableField("VERSION")
    private Integer version;

    @TableField("TIMESTAMP")
    private LocalDateTime timestamp;

    @TableField("ACCOUNT_ID")
    private Long accountID;

    @TableField("PERMISSION")
    private Integer permission;

    @TableField("STRATEGY_ID")
    private Long strategyId;
}
