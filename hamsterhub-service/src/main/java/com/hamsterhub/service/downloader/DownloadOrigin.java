package com.hamsterhub.service.downloader;

public enum DownloadOrigin {

    USER(1), // 用户手动发起
    RSS(2); // 来自RSS订阅自动发起

    private final int value;

    DownloadOrigin(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DownloadOrigin fromValue(int value) {
        for (DownloadOrigin enumObj : DownloadOrigin.values()) {
            if (enumObj.getValue() == value) {
                return enumObj;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
