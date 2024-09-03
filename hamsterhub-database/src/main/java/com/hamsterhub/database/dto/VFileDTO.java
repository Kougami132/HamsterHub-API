package com.hamsterhub.database.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description="")
public class VFileDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private String id;

    @Schema(description = "文件类型")
    private Integer type;

    @Schema(description = "文件名")
    private String name;

    @Schema(description = "父文件ID")
    private Long parentId;

    @Schema(description = "文件版本")
    private Integer version;

    @Schema(description = "文件创建时间")
    private Long created;

    @Schema(description = "文件修改时间")
    private Long modified;

    @Schema(description = "文件所有人ID")
    private Long userId;

    @Schema(description = "文件大小")
    private Long size;

    @Schema(description = "存储策略ID")
    private Long strategyId;

    @Schema(description = "分享类型")
    private Integer shareType;

    @Schema(description = "hash")
    private String hash;

    public Boolean isDir() {
        return this.type.equals(0);
    }

    public static VFileDTO rootFileDTO() {
        VFileDTO vFileDTO = new VFileDTO();
        vFileDTO.setId("0");
        vFileDTO.setParentId(0L);
        vFileDTO.setType(0);
        return vFileDTO;
    }

    public static VFileDTO rootFileDTO(Long userId, Long strategyId) {
        VFileDTO vFileDTO = rootFileDTO();
        vFileDTO.setUserId(userId);
        vFileDTO.setStrategyId(strategyId);
        return vFileDTO;
    }

    public static VFileDTO newFile(String name, Long strategyId, Long parentId, RFileDTO rFileDTO, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        VFileDTO file = new VFileDTO(null, 1, name, parentId, 0,
                toTimestamp(now) , toTimestamp(now), userId, rFileDTO.getSize(), strategyId, 0,"");
        return file;
    }

    public static VFileDTO newFile(String name, Long strategyId, Long parentId, Long size, Long userId,String hash) {
        LocalDateTime now = LocalDateTime.now();
        VFileDTO file = new VFileDTO(null, 1, name, parentId, 0,
                toTimestamp(now), toTimestamp(now), userId, size, strategyId, 0,hash);
        return file;
    }

    public static VFileDTO newDir(String name, Long strategyId, Long parentId, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        VFileDTO dir = new VFileDTO(null, 0, name, parentId, 0, toTimestamp(now),
                toTimestamp(now), userId, 0L, strategyId, 0,"");
        return dir;
    }

    public static Long toTimestamp(LocalDateTime date){
        return date.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
