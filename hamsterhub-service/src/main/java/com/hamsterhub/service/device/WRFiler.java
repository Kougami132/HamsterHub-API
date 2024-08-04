package com.hamsterhub.service.device;

import com.hamsterhub.service.dto.DeviceDTO;

import java.io.File;

public interface WRFiler {

//    public Storage withDevice(DeviceDTO device);

    // 返回path
    public String upload(File file, String name, String hash);

    // 返回下载直链，本地则返回文件路径
    public String downLoad(String url);

    // 用于删除文件
    public void delete(String url) ;

    // 获取总计存储空间 单位: Bytes
    public Long getTotalSize();

    // 获取可用存储空间 单位: Bytes
    public Long getUsableSize();

    public boolean verify(DeviceDTO deviceDTO);

    public boolean isConnected();
}
