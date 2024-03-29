package com.hamsterhub.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(value="VFileDTO", description="")
public class VFileDTO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "文件类型")
    private Integer type;

    @ApiModelProperty(value = "文件名")
    private String name;

    @ApiModelProperty(value = "父文件ID")
    private Long parentId;

    @ApiModelProperty(value = "实际文件ID")
    private Long rFileId;

    @ApiModelProperty(value = "文件版本")
    private Integer version;

    @ApiModelProperty(value = "文件创建时间")
    private LocalDateTime created;

    @ApiModelProperty(value = "文件修改时间")
    private LocalDateTime modified;

    @ApiModelProperty(value = "文件所有人ID")
    private Long accountID;

    @ApiModelProperty(value = "文件大小")
    private Long size;

    @ApiModelProperty(value = "存储策略ID")
    private Long strategyId;

    @ApiModelProperty(value = "分享类型")
    private Integer shareType;

    public Boolean isDir() {
        return this.type.equals(0);
    }

    public static VFileDTO rootFileDTO() {
        VFileDTO vFileDTO = new VFileDTO();
        vFileDTO.setId(0L);
        vFileDTO.setParentId(0L);
        vFileDTO.setType(0);
        return vFileDTO;
    }
}
