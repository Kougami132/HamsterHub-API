package com.hamsterhub.database.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("rss_task")
public class RSSTask implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("USER_ID")
    private Long userId;

    @TableField("RSS_LIST_ID")
    private Long rssListId;

    @TableField("TITLE")
    private String title;

    @TableField("URL")
    private String url;

    @TableField("PUB_DATE")
    private LocalDateTime pubDate;

    @TableField("SIZE")
    private Long size;

    @TableField("STATE")
    private Integer state;

}
