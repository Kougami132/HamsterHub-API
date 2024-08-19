package com.hamsterhub.database.enums;

public enum DownloadState {
    WAIT(0), // 等待排队
    DOWNLOADING(1), // 正在下载
    ERROR(2), // 下载出错
    FINISH(3);

    private final int value;

    DownloadState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DownloadState fromValue(int value) {
        for (DownloadState enumObj : DownloadState.values()) {
            if (enumObj.getValue() == value) {
                return enumObj;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
