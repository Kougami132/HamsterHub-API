package com.hamsterhub.device;

import com.hamsterhub.service.dto.DeviceDTO;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class Storage {
    private Integer code;
    private String name;
    private DeviceDTO device;

    // 返回绑定设备的实例
    public Storage withDevice(DeviceDTO device) {
        return null;
    }

    // 返回path
    public String upload(MultipartFile file, String name) {
        return null;
    }

    // 返回下载直链，本地则返回文件路径
    public String downLoad(String url) {
        return null;
    }

    public void delete(String url) {

    }

    // 单位: Bytes
    public Long getTotalSize() {
        return null;
    }

    public Long getUsableSize() {
        return null;
    }

    public boolean verify() {
        return true;
    }

}
