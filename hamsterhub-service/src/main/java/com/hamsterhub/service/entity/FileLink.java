package com.hamsterhub.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_link")
public class FileLink {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ID_WORKER)
    private Long id;

    @TableField("TICKET")
    private String ticket;

    @TableField("R_FILE_ID")
    private Long rFileId;

    @TableField("`EXPIRY`")
    private LocalDateTime expiry;
}
