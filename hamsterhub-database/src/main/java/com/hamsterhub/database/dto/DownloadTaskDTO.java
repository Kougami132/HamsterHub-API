package com.hamsterhub.database.dto;

import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.database.enums.DownloadOrigin;
import com.hamsterhub.database.enums.DownloadState;
import com.hamsterhub.database.enums.DownloadType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description="")
public class DownloadTaskDTO implements Serializable {

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

    @Schema(description = "对应任务在下载器中的索引")
    private String taskIndex;

    @Schema(description = "完成的进度")
    private Long completed;

    @Schema(description = "总进度")
    private Long total;

    public static DownloadTaskDTO createTask(String root, String parentIndex,
                                             Integer originType, Long originId,
                                             Integer type, String url, Integer downloader,
                                             Long userId, String tag, String name){

        DownloadTaskDTO dto = new DownloadTaskDTO();
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
        dto.setName (name);
        dto.setTaskIndex("");
        return dto;
    }

    public static DownloadTaskDTO createTask(RSSListDTO rssListDTO, RSSTaskDTO rssTaskDTO, String parentIndex){

        DownloadTaskDTO dto = new DownloadTaskDTO();
        dto.setName(rssTaskDTO.getTitle());
        dto.setRoot(rssListDTO.getRoot());
        dto.setParentIndex (parentIndex);
        dto.setOriginType(DownloadOrigin.RSS.ordinal());
        dto.setOriginId (rssTaskDTO.getId());
        dto.setType(DownloadType.TORRENT_URL.ordinal());
        dto.setUrl (rssTaskDTO.getUrl());
        dto.setDownloader(rssListDTO.getDownloader());
        dto.setState(DownloadState.WAIT.ordinal());
        dto.setUserId (rssTaskDTO.getUserId());
        dto.setTag(StringUtil.generateRandomString(16));
        dto.setTaskIndex("");
        return dto;
    }

}
