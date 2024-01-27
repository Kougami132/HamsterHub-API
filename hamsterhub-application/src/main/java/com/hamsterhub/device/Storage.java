package com.hamsterhub.device;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MatchUtil;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.entity.Device;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@Data
public class Storage {
    private Integer code;
    private String name;
    private DeviceDTO device;

    public Storage(DeviceDTO deviceDTO) {
        // json格式校验
        if (!MatchUtil.isJson(deviceDTO.getParam()))
            throw new BusinessException(CommonErrorCode.E_300005);
    }

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

    public boolean verify(DeviceDTO deviceDTO) {
        return true;
    }

    public boolean isConnected() {
        return this.getDevice().isConnected();
    }
}
