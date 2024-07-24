package com.hamsterhub.service.device;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MatchUtil;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.dto.VFileDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;

@NoArgsConstructor
@Data
public class ListStorage extends Storage {
    private Integer code;
    private String name;
    private DeviceDTO device = null;
    private Boolean ready = false;

    public ListStorage(DeviceDTO deviceDTO) {
        // json格式校验
        if (!MatchUtil.isJson(deviceDTO.getParam()))
            throw new BusinessException(CommonErrorCode.E_300005);
    }

    // 返回path
    public String upload(File file, String name) {
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

    public VFileDTO queryFile(String fileIndex){
        return null;
    }

    public List<VFileDTO> queryDirectory(String parentIndex, Integer page, Integer limit){
        return null;
    }

    public VFileDTO makeDirectory(String parentIndex, String name){
        return null;
    }

    public void deleteByPath(String fileIndex) {}

    // 上传到指定位置，与upload不同，该方法为RealyStrategyStorage类实现
    public VFileDTO uploadTo(String parentIndex, File file, String name) {
        return null;
    }

    public String downLoadByPath(String fileIndex){
        return null;
    }

    public void rename(String index, String name){}

    public void copyTo(String index, String parent){}

    public void moveTo(String index, String parent){}
}
