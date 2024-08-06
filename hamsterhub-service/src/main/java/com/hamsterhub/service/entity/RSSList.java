package com.hamsterhub.service.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("rss_list")
public class RSSList implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "ID", type = IdType.ASSIGN_ID)
    private Long id;

    @TableField("URL")
    private String url;

    @TableField("USER_ID")
    private Long userId;

    @TableField("ROOT")
    private String root;

    @TableField("PARENT_INDEX")
    private String parentIndex;

    @TableField("NAME")
    private String name;

    @TableField("STATE")
    private Integer state;

    @TableField("LAST_HASH")
    private String lastHash;

    @TableField("REPLACE_HOST")
    private String replaceHost;

    @TableField("MIRROR_HOST")
    private String mirrorHost;

}
