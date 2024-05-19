package com.hamsterhub.service.entity;

import lombok.Data;

@Data
public class Torrent {
    private Long added_on;
    private Long amount_left;
    private Boolean auto_tmm;
    private Double availability;
    private String category;
    private Long completed;
    private Long completion_on;
    private String content_path;
    private Long dl_limit;
    private Long dlspeed;
    private String download_path;
    private Long downloaded;
    private Long downloaded_session;
    private Long eta;
    private Boolean f_l_piece_prio;
    private Boolean force_start;
    private String hash;
    private String infohash_v1;
    private String infohash_v2;
    private Long last_activity;
    private String magnet_uri;
    private Long max_ratio;
    private Long max_seeding_time;
    private String name;
    private Long num_complete;
    private Long num_incomplete;
    private Long num_leechs;
    private Long num_seeds;
    private Long priority;
    private Double progress;
    private Long ratio;
    private Long ratio_limit;
    private String save_path;
    private Long seeding_time;
    private Long seeding_time_limit;
    private Long seen_complete;
    private Boolean seq_dl;
    private Long size;
    private String state;
    private Boolean super_seeding;
    private String tags;
    private Long time_active;
    private Long total_size;
    private String tracker;
    private Long trackers_count;
    private Long up_limit;
    private Long uploaded;
    private Long uploaded_session;
    private Long upspeed;

    public Boolean isCompleted() {
        return !completed.equals(0L) && completed.equals(total_size);
    }
}
