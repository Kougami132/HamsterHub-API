package com.hamsterhub.service.service.impl;

import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.GetBeanUtil;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.device.ListFiler;
import com.hamsterhub.service.device.impl.RealStrategyStorage;
import com.hamsterhub.service.device.impl.VirtualStrategyStorage;
import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.database.dto.VFileDTO;
import com.hamsterhub.database.entity.Strategy;

import com.hamsterhub.database.mapper.StrategyMapper;
import com.hamsterhub.database.service.DeviceService;
import com.hamsterhub.service.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {
    public static final Integer VIRTUAL_FILE_SYSTEM = 0;
    public static final Integer REALY_FILE_SYSTEM = 1;

    @Autowired
    private StrategyMapper strategyMapper;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private GetBeanUtil getBeanUtil;

    private Map<String,ListFiler> strategies = new HashMap<>();

    @Override
    @PostConstruct
    public void loadData(){
        List<Strategy> entities = strategyMapper.selectList(null);

        for (Strategy temp : entities) {
            String rootName = temp.getRoot();

            Integer fileSystemType = temp.getFileSystem();
            ListFiler filer = null;

            if(fileSystemType == VIRTUAL_FILE_SYSTEM){
                filer = new VirtualStrategyStorage(temp);
            }else if (fileSystemType == REALY_FILE_SYSTEM){
                filer = new RealStrategyStorage(temp);
            }

            if(filer != null){
                strategies.put(rootName,filer);
            }

        }
    }

    @Override
    public ListFiler getListFiler(String root) {
        ListFiler listFiler = strategies.get(root);
        CommonErrorCode.checkAndThrow(listFiler == null,CommonErrorCode.E_400001);
        CommonErrorCode.checkAndThrow(!listFiler.isReady(),CommonErrorCode.E_400007);
        return listFiler;
    }

    @Override
    public Boolean isExist(String root, String index) {
        ListFiler listFiler = getListFiler(root);
        return listFiler.isExist(index);
    }

    @Override
    public List<VFileDTO> queryFile(String root, String url, UserDTO userDTO){
        ListFiler listFiler = getListFiler(root);
        return listFiler.queryFile(url,userDTO.getId());
    }

    @Override
    public VFileDTO getFile(String root, String index, UserDTO userDTO){
        ListFiler listFiler = getListFiler(root);
        return listFiler.getFile(index,userDTO.getId());
    }

    @Override
    public List<VFileDTO> queryDirectory(String root, String parent, UserDTO userDTO, Integer page, Integer limit) {
        ListFiler listFiler = getListFiler(root);
        return listFiler.queryDirectory(parent, userDTO.getId(), page, limit);
    }

    @Override
    public VFileDTO makeDirectory(String root, String parent, String name, UserDTO userDTO) {
        ListFiler listFiler = getListFiler(root);
        return listFiler.makeDirectory(parent, name, userDTO.getId());
    }

    @Override
    public Integer queryFileCount(String root, String index) {
        ListFiler listFiler = getListFiler(root);
        return listFiler.queryFileCount(index);
    }

    @Override
    public void delete(String root, String index, UserDTO userDTO) {
        ListFiler listFiler = getListFiler(root);
        listFiler.delete(index, userDTO.getId());
    }

    @Override
    public void rename(String root, String index, String name, UserDTO userDTO) {
        ListFiler listFiler = getListFiler(root);
        listFiler.rename(index, name, userDTO.getId());
    }

    @Override
    public void copyTo(String root, String index, String parent, UserDTO userDTO) {
        ListFiler listFiler = getListFiler(root);
        listFiler.copyTo(index, parent, userDTO.getId());
    }

    @Override
    public void moveTo(String root, String index, String parent, String name, UserDTO userDTO) {
        ListFiler listFiler = getListFiler(root);
        listFiler.moveTo(index, parent, name, userDTO.getId());
    }

    @Override
    public void uploadBefore(String root, String parent,String name, UserDTO userDTO) {
        ListFiler listFiler = getListFiler(root);
        listFiler.uploadBefore(parent, name, userDTO.getId());
    }

    @Override
    public VFileDTO upload(String root, File file, String parent, String name, Long size, String hash, UserDTO userDTO) {
        ListFiler listFiler = getListFiler(root);

        String md5 = null;

        if (StringUtil.isBlank(hash)){
            md5 = MD5Util.getMd5(file);
        }else{
            md5 = hash;
        }
        return listFiler.upload(parent, file, name, size, userDTO.getId(), md5);
    }

    @Override
    public String getDownloadUrl(String root, String index, UserDTO userDTO, Long preference){
        ListFiler listFiler = getListFiler(root);
        return listFiler.getDownloadUrl(index, userDTO.getId(), preference);
    }

    @Override
    public Long getTotalSize(String root, Integer combineOption) {
        ListFiler listFiler = getListFiler(root);
        return listFiler.getTotalSize(combineOption);
    }

    @Override
    public Long getUsableSize(String root, Integer combineOption) {
        ListFiler listFiler = getListFiler(root);
        return listFiler.getUsableSize(combineOption);
    }

    @Override
    public String getQueryUrl(String root, String index, UserDTO userDTO){
        ListFiler listFiler = getListFiler(root);
        return listFiler.getQueryUrl(index, userDTO.getId());
    }




}
