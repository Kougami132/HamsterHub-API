package com.hamsterhub.service.device;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MatchUtil;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.dto.RFileDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@NoArgsConstructor
@Data
public class Storage implements WRFiler {
    private Integer code;
    private String name;
    private DeviceDTO device = null;
    private Boolean ready = false;

    public String downloadIndexAdapt(RFileDTO rFileDTO){
        // 默认使用path的内容作为文件的索引
        return rFileDTO.getPath();
    }

    public Storage(DeviceDTO deviceDTO) {
        // json格式校验
        if (!MatchUtil.isJson(deviceDTO.getParam()))
            throw new BusinessException(CommonErrorCode.E_300005);
    }

    // 返回path
    public String upload(File file, String name, String hash) {
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
