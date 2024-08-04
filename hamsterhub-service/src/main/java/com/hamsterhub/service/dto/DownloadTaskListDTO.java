package com.hamsterhub.service.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.hamsterhub.service.downloader.DownloadState;
import com.hamsterhub.service.entity.DownloadTaskList;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description="")
public class DownloadTaskListDTO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "文件名称")
    private String name;

    @Schema(description = "状态")
    private Integer state;

    @Schema(description = "调用的下载器")
    private Integer downloader;

    @Schema(description = "下载类型")
    private Integer type;

    @Schema(description = "下载的目标")
    private String url;

    @Schema(description = "下载任务的来源")
    private Integer originType;

    @Schema(description = "下载任务的来源的主键")
    private Long originId;

    @Schema(description = "下载完成后存储的策略")
    private String root;

    @Schema(description = "下载完成后存储的父目录")
    private String parentIndex;

    @Schema(description = "所属用户id")
    private Long userId;

    @Schema(description = "总进度")
    private String tag;

    @Schema(description = "完成的进度")
    private Long completed;

    @Schema(description = "总进度")
    private Long total;

    public static DownloadTaskListDTO createTask(String root, String parentIndex,
                                                  Integer originType, Long originId,
                                                  Integer type, String url,
                                                  Integer downloader, Long userId, String tag){

        DownloadTaskListDTO dto = new DownloadTaskListDTO();
        dto.setRoot (root);
        dto.setParentIndex (parentIndex);
        dto.setOriginType (originType);
        dto.setOriginId (originId);
        dto.setType (type);
        dto.setUrl (url);
        dto.setDownloader (downloader);
        dto.setState(DownloadState.WAIT.ordinal());
        dto.setUserId (userId);
        dto.setTag (tag);
        return dto;
    }
}
