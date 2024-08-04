package com.hamsterhub.service.downloader;

public enum DownloadType {
    URL(1),
    MAGNET(2),
    TORRENT_URL(3);

    private final int value;

    DownloadType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DownloadType fromValue(int value) {
        for (DownloadType downloadType : DownloadType.values()) {
            if (downloadType.getValue() == value) {
                return downloadType;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }
}
