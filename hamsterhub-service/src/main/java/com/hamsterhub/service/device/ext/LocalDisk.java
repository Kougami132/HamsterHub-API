package com.hamsterhub.service.device.ext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hamsterhub.common.util.GetBeanUtil;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.device.Storage;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.dto.FileLinkDTO;
import com.hamsterhub.service.service.DeviceService;
import com.hamsterhub.service.service.FileLinkService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@Data
public class LocalDisk extends Storage {

    private Integer code = 0;
    private String name = "本地";
    private DeviceDTO device;
    private String path = "";

    private FileLinkService fileLinkService;

    public LocalDisk(DeviceDTO deviceDTO) {
        super(deviceDTO);
        this.device = deviceDTO;

        JSONObject param = JSON.parseObject(deviceDTO.getParam());
        if (param != null)
            this.path = param.getString("path");

        fileLinkService = GetBeanUtil.getBean(FileLinkService.class);

        this.device.setConnected(true);
    }

    @Override
    public String upload(File file, String name) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("/yyyy/MM/dd"));
        File dir = new File(path + "uploads" + today);
        if (!dir.exists()) dir.mkdirs();
        String hash = MD5Util.getMd5(file);
        String url = dir.getAbsolutePath() + File.separator + hash;
        try {
            file.renameTo(new File(url));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }

    @Override
    public String downLoad(String url) {
        // 传入的变量应当是 rFileDTO id

        Long rFileDTOId = Long.parseLong(url);
        FileLinkDTO fileLinkDTO;
        String ticket;
        if (fileLinkService.isExist(rFileDTOId)) { // 文件直链已存在
            fileLinkDTO = fileLinkService.query(rFileDTOId);
            if (fileLinkDTO.getExpiry().isBefore(LocalDateTime.now())) { // 直链已过期
                do {
                    fileLinkDTO.setTicket(StringUtil.generateRandomString(10));
                }
                while (fileLinkService.isExist(fileLinkDTO.getTicket()));
            }
            fileLinkDTO.setExpiry(LocalDateTime.now().plusMinutes(10));
            fileLinkService.update(fileLinkDTO);
        }
        else {
            do {
                ticket = StringUtil.generateRandomString(10);
            }
            while (fileLinkService.isExist(ticket));

            fileLinkDTO = new FileLinkDTO(ticket, rFileDTOId, LocalDateTime.now().plusMinutes(10));
            fileLinkService.create(fileLinkDTO);
        }
        return String.format("/download?ticket=%s", fileLinkDTO.getTicket());
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
        File dir = new File(path + "uploads");
        if (!dir.exists()) dir.mkdirs();
        return dir.getTotalSpace();
    }

    @Override
    public Long getUsableSize() {
        File dir = new File(path + "uploads");
        if (!dir.exists()) dir.mkdirs();
        return dir.getUsableSpace();
    }

}
