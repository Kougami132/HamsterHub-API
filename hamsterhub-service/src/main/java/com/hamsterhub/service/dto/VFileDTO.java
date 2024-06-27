package com.hamsterhub.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description="")
public class VFileDTO {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "文件类型")
    private Integer type;

    @Schema(description = "文件名")
    private String name;

    @Schema(description = "父文件ID")
    private Long parentId;

    @Schema(description = "实际文件ID")
    private Long rFileId;

    @Schema(description = "文件版本")
    private Integer version;

    @Schema(description = "文件创建时间")
    private LocalDateTime created;

    @Schema(description = "文件修改时间")
    private LocalDateTime modified;

    @Schema(description = "文件所有人ID")
    private Long accountID;

    @Schema(description = "文件大小")
    private Long size;

    @Schema(description = "存储策略ID")
    private Long strategyId;

    @Schema(description = "分享类型")
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

    public static VFileDTO newFile(String name, Long strategyId, Long parentId, RFileDTO rFileDTO, Long accountId) {
        VFileDTO file = new VFileDTO(null, 1, name, parentId, rFileDTO.getId(), 0,
                LocalDateTime.now(), LocalDateTime.now(), accountId, rFileDTO.getSize(), strategyId, 0);
        return file;
    }

    public static VFileDTO newDir(String name, Long strategyId, Long parentId, Long accountId) {
        VFileDTO dir = new VFileDTO(null, 0, name, parentId, 0L, 0, LocalDateTime.now(),
                LocalDateTime.now(), accountId, 0L, strategyId, 0);
        return dir;
    }
}
