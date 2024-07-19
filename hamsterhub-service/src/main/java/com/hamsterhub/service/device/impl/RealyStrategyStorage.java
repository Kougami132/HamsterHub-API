package com.hamsterhub.service.device.impl;

import com.hamsterhub.common.util.GetBeanUtil;
import com.hamsterhub.service.device.ListFiler;
import com.hamsterhub.service.device.Storage;
import com.hamsterhub.service.device.ext.AliDrive;
import com.hamsterhub.service.device.ext.LocalDisk;
import com.hamsterhub.service.device.ext.OneDrive;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.dto.VFileDTO;
import com.hamsterhub.service.entity.Strategy;
import com.hamsterhub.service.service.DeviceService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RealyStrategyStorage implements ListFiler {

    private DeviceService deviceService;

    private Long id;
    private String name;
    private Integer type;
    private Integer mode;
    private Integer permission;
    private String root;
    private Integer fileSystem;
    private String param;
    private Integer[] priority;
    private List<Storage> devices = new ArrayList<>();
    private Boolean ready = false;



    public RealyStrategyStorage(Strategy strategy){
        this.id = strategy.getId();
        this.name = strategy.getName();
        this.type = strategy.getType();
        this.mode = strategy.getMode();
        this.permission = strategy.getPermission();
        this.root = strategy.getRoot();
        this.fileSystem = strategy.getFileSystem();
        this.param = strategy.getParam();

        this.deviceService = GetBeanUtil.getBean(DeviceService.class);
        this.init();
    }

    public void init() {
        devices.add(createDevices(param));
        this.ready = true;
    }

    public Boolean isReady() {
        return  this.ready;
    }

    public Storage createDevices(String deviceId) {
        DeviceDTO deviceDTO = this.deviceService.query(Long.parseLong(deviceId));

        // 默认Storage是未就绪的
        Storage res = new Storage();
        if (deviceDTO != null){
            Integer deviceType = deviceDTO.getType();

            switch (deviceType){
                case 0:
                    res = new LocalDisk(deviceDTO);
                    break;
                case 1:
                    res = new AliDrive(deviceDTO);
                    break;
                case 2:
                    res = new OneDrive();
                    break;
            }
        }
        return res;
    }

    @Override
    public Boolean isExist(String index) {
        return null;
    }

    @Override
    public List<VFileDTO> queryFile(String url, Long userId) {
        return null;
    }

    @Override
    public List<VFileDTO> queryDirectory(String parentId, Long userId, Integer page, Integer limit){
        // 暂时不考虑分页
        return null;
    }

    @Override
    public VFileDTO makeDirectory(String parent, String name, Long userId) {
        return null;
    }

    @Override
    public Integer queryFileCount(String index) {
        return 0;
    }

    @Override
    public void delete(String index, Long userId) {

    }

    @Override
    public void rename(String index, String name, Long userId) {
        return ;
    }

    @Override
    public void copyTo(String index, String parent, Long userId) {
        return ;
    }

    @Override
    public void moveTo(String index, String parent, Long userId) {
        return ;
    }

    @Override
    public void uploadBefore(String parent, String name, Long userId) {
        return ;
    }

    @Override
    public VFileDTO upload(String parent, File file, String name, Long size, Long userId, String hash) {
        return null;
    }

    @Override
    public String getDownloadUrl(String index, Long userId, Long preference) {
        return "";
    }

    @Override
    public Integer getCombineNumber() {
        return 1;
    }

    @Override
    public Long getTotalSize(Integer combineOption) {
        return 0L;
    }

    @Override
    public Long getUsableSize(Integer combineOption) {
        return 0L;
    }

}
