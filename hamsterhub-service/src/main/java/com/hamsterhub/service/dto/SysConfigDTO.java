package com.hamsterhub.service.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@ApiModel(value="SysConfigDTO", description="")
public class SysConfigDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private String key;

    @ApiModelProperty(value = "值")
    private String value;

    @ApiModelProperty(value = "排序id")
    private Integer orderID ;

    @ApiModelProperty(value = "数据类型，决定前端的输入和显示形式")
    private String type;

    @ApiModelProperty(value = "是否隐藏，为真之后对应配置不会发给前端")
    private Boolean hide;

    public SysConfigDTO(String key, String value, Integer orderID, String type, Boolean hide) {
        this.key = key;
        this.value = value;
        this.orderID = orderID;
        this.type = type;
        this.hide = hide;
    }

    public SysConfigDTO(String key, String value) {
        this.key = key;
        this.value = value;
        this.orderID = null;
        this.type = null;
        this.hide = null;
    }

}