package com.hamsterhub.service.device.impl;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.GetBeanUtil;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.device.ListFiler;
import com.hamsterhub.service.device.ListStorage;
import com.hamsterhub.service.device.Storage;
import com.hamsterhub.service.device.ext.AliDrive;
import com.hamsterhub.service.device.ext.LocalDisk;
import com.hamsterhub.service.device.ext.OneDrive;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.dto.VFileDTO;
import com.hamsterhub.service.entity.Strategy;
import com.hamsterhub.service.service.DeviceService;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RealyStrategyStorage implements ListFiler {
    public static final Integer fileSystemType = 1;

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
    private List<ListStorage> devices = new ArrayList<>();
    private Boolean ready = false;

    Pattern lastPartOfPathPattern = Pattern.compile(".*/([^/]+)$");

    public String getLastPartOfPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        Matcher matcher = lastPartOfPathPattern.matcher(path);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }



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
        this.ready = !devices.isEmpty();
    }

    public Boolean isReady() {
        return  this.ready;
    }

    public ListStorage createDevices(String deviceId) {
        DeviceDTO deviceDTO = this.deviceService.query(Long.parseLong(deviceId));

        // 默认Storage是未就绪的
        ListStorage res = new ListStorage();
        if (deviceDTO != null){
            Integer deviceType = deviceDTO.getType();

            switch (deviceType){
                case 0:
                    res = new LocalDisk(deviceDTO);
                    break;
//                case 1:
//                    res = new AliDrive(deviceDTO);
//                    break;
//                case 2:
//                    res = new OneDrive();
//                    break;
            }
        }
        return res;
    }

    @Override
    public Integer getFileSystem() {
        return fileSystem;
    }

    @Override
    public Boolean isExist(String index) {
        ListStorage listStorage = devices.get(0);
        // todo
        return false;
    }

    public String pathCheck(String path){
        if (path == null || "0".equals(path)){
            return "";
        }
        return path.replace("../","");
    }

    @Override
    public List<VFileDTO> queryFile(String url, Long userId) {
        String filePath = pathCheck(url);
        ListStorage listStorage = devices.get(0);
        List<VFileDTO> res = new ArrayList<>();
        res.add(listStorage.queryFile(filePath));
        return res;
    }

    @Override
    public VFileDTO getFile(String index, Long userId) {
        String filePath = pathCheck(index);
        ListStorage listStorage = devices.get(0);
        return listStorage.queryFile(filePath);
    }

    @Override
    public List<VFileDTO> queryDirectory(String parentId, Long userId, Integer page, Integer limit){
        // 暂时不考虑分页
        String filePath = pathCheck(parentId);
        ListStorage listStorage = devices.get(0);
        return listStorage.queryDirectory(filePath,page,limit);
    }

    @Override
    public VFileDTO makeDirectory(String parent, String name, Long userId) {
        String filePath = pathCheck(parent);
        ListStorage listStorage = devices.get(0);
        return listStorage.makeDirectory(filePath,name);
    }

    @Override
    public Integer queryFileCount(String index) {
        return 0;
    }

    @Override
    public void delete(String index, Long userId) {
        String filePath = pathCheck(index);
        ListStorage listStorage = devices.get(0);
        listStorage.deleteByPath(filePath);
    }

    @Override
    public void rename(String index, String name, Long userId) {
        String filePath = pathCheck(index);
        ListStorage listStorage = devices.get(0);
        listStorage.rename(filePath, name);
    }

    @Override
    public void copyTo(String index, String parent, Long userId) {
        String filePath = pathCheck(index);
        String parentPath = pathCheck(parent);
        ListStorage listStorage = devices.get(0);
        listStorage.copyTo(filePath, parentPath);
    }

    @Override
    public void moveTo(String index, String parent, String name, Long userId) {
        String filePath = pathCheck(index);
        String parentPath = pathCheck(parent);
        ListStorage listStorage = devices.get(0);
        listStorage.moveTo(filePath, parentPath, name);
    }

    @Override
    public void uploadBefore(String parent, String name, Long userId) {
        return ;
    }

    @Override
    public VFileDTO upload(String parent, File file, String name, Long size, Long userId, String hash) {
        String filePath = pathCheck(parent);
        ListStorage listStorage = devices.get(0);
        return listStorage.uploadTo(filePath,file,name);
    }

    @Override
    public String getDownloadUrl(String index, Long userId, Long preference){
        String filePath = pathCheck(index);
        ListStorage listStorage = devices.get(0);
        String fileName = getLastPartOfPath(index);
        if (StringUtil.isBlank(fileName)){
            fileName = "file";
        }
        String url = listStorage.downLoadByPath(filePath);

        if (listStorage.getDevice().getType().equals(0)){ // 本地硬盘时，为统一接口，不把东西传进去
            try{
                url = url + "&fileName=" + StringUtil.encodeUrl(fileName); // 防止文件名导致编码异常
            } catch (UnsupportedEncodingException e) {
                throw new BusinessException(CommonErrorCode.UNKNOWN);
            }

        }


        return url;
    }

    @Override
    public Integer getCombineNumber() {
        return 1;
    }

    @Override
    public Long getTotalSize(Integer combineOption) {
        ListStorage listStorage = devices.get(0);
        return listStorage.getTotalSize();
    }

    @Override
    public Long getUsableSize(Integer combineOption) {
        ListStorage listStorage = devices.get(0);
        return listStorage.getUsableSize();
    }

    @Override
    public String getQueryUrl(String index, Long userId) {
        VFileDTO file = this.getFile(index, userId);
        return file.getId();
    }

}
