package com.hamsterhub.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("r_file")
public class RFile implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("NAME")
    private String name;

    @TableField("HASH")
    private String hash;

    @TableField("PATH")
    private String path;

    @TableField("SIZE")
    private Long size;

    @TableField("DEVICE_ID")
    private Long deviceId;
}
