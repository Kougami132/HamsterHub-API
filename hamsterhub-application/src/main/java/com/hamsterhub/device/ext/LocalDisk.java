package com.hamsterhub.device.ext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.common.util.MatchUtil;
import com.hamsterhub.device.Storage;
import com.hamsterhub.service.dto.DeviceDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Component
public class LocalDisk extends Storage {

    private Integer code = 0;
    private String name = "本地";
    private DeviceDTO device;
    private String path;

    @Override
    public LocalDisk withDevice(DeviceDTO device) {
        // json格式校验
        if (!MatchUtil.isJson(device.getParam()))
            throw new BusinessException(CommonErrorCode.E_300005);
        JSONObject param = JSON.parseObject(device.getParam());
        String path = param.getString("param");
        return new LocalDisk(this.code, this.name, device, path);
    }

    @Override
    public String upload(MultipartFile file, String name) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("/yyyy/MM/dd"));
        File dir = new File(path + "uploads" + today);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String hash = MD5Util.getMd5(file);
        String url = dir.getAbsolutePath() + File.separator + hash;
        try {
            file.transferTo(new File(url));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    @Override
    public String downLoad(String url) {
        return url;
    }

    @Override
    public void delete(String url) {
        File file = new File(url);
        try {
            file.delete();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Long getTotalSize() {
        File dir = new File("uploads");
        return dir.getTotalSpace();
    }

    @Override
    public Long getUsableSize() {
        File dir = new File("uploads");
        return dir.getUsableSpace();
    }

    @Override
    public boolean verify() {
        return super.verify();
    }
}
