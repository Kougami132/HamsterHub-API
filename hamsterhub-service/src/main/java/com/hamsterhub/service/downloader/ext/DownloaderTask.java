package com.hamsterhub.service.downloader.ext;

import lombok.Data;

@Data
public class DownloaderTask {

    private Long amount_left;
    private Long completed;
    private String hash;
    private String name;
    private String save_path;
    private String state;
    private String tags;
    private String taskIndex;
    private Long total_size;

    public Boolean isCompleted() {
        return !completed.equals(0L) && completed.equals(total_size);
    }

}
