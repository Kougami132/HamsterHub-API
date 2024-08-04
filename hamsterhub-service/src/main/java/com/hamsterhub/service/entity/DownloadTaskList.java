package com.hamsterhub.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("download_task")
public class DownloadTaskList implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("NAME")
    private String name;

    @TableField("STATE")
    private Integer state;

    @TableField("DOWNLOADER")
    private Integer downloader;

    @TableField("TYPE")
    private Integer type;

    @TableField("URL")
    private String url;

    @TableField("ORIGIN_TYPE")
    private Long originType;

    @TableField("ORIGIN_ID")
    private Long originId;

    @TableField("ROOT")
    private String root;

    @TableField("PARENT_INDEX")
    private String parentIndex;

    @TableField("USER_ID")
    private String userId;

    @TableField("TAG")
    private String tag;

}
