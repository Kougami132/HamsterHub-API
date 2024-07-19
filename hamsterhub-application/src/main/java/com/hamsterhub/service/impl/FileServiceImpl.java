package com.hamsterhub.service.impl;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.service.device.Storage;
import com.hamsterhub.service.FileService;
import com.hamsterhub.service.StorageService;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.dto.RFileDTO;
import com.hamsterhub.service.dto.StrategyDTO;
import com.hamsterhub.service.service.DeviceService;
import com.hamsterhub.service.service.DeviceStrategyService;
import com.hamsterhub.service.service.RFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
public class FileServiceImpl implements FileService {

    @Autowired
    private StorageService storageService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private DeviceStrategyService deviceStrategyService;
    @Autowired
    private RFileService rFileService;

    @Override
    public String getHash(File file) throws BusinessException {
        return MD5Util.getMd5(file);
    }

    // 选择设备算法
    private DeviceDTO switchDevice(List<DeviceDTO> deviceDTOs, Integer mode, Long size) {
        DeviceDTO result = null;
        if (mode.equals(0)) { // 优先选择剩余容量大
            Long max = 0L;
            for (DeviceDTO i: deviceDTOs) {
                Storage storage = storageService.getInstance(i);
                if (storage.getUsableSize() > max) {
                    max = storage.getUsableSize();
                    if (max > size)
                        result = i;
                }
            }
        }
        else if (mode.equals(1)) { // 优先选择剩余容量小
            Long min = Long.MAX_VALUE;
            for (DeviceDTO i: deviceDTOs) {
                Storage storage = storageService.getInstance(i);
                if (storage.getUsableSize() < min && storage.getUsableSize() > size) {
                    min = storage.getUsableSize();
                    result = i;
                }
            }
        }
        return result;
    }

    @Override
    public RFileDTO upload(File file, StrategyDTO strategyDTO) throws BusinessException {
        String hash = this.getHash(file);
        // 实际文件已存在
        if (rFileService.isExist(hash, strategyDTO.getId()))
            return rFileService.query(hash, strategyDTO.getId());
        List<Long> deviceIds = deviceStrategyService.queryDeviceIds(strategyDTO.getId());
        List<DeviceDTO> deviceDTOs = new ArrayList<>();
        for (Long i: deviceIds)
            deviceDTOs.add(deviceService.query(i));
        // 选择设备算法
        DeviceDTO deviceDTO = switchDevice(deviceDTOs, strategyDTO.getMode(), file.length());
        // 设备空间不足
        if (deviceDTO == null)
            throw new BusinessException(CommonErrorCode.E_300007);
        Storage storage = storageService.getInstance(deviceDTO);
        // 上传文件
        String url = storage.upload(file, hash);
        RFileDTO rFileDTO = new RFileDTO(null, hash, hash, url, file.length(), deviceDTO.getId());
        rFileDTO = rFileService.create(rFileDTO);
        return rFileDTO;
    }

    @Override
    public String download(RFileDTO rFileDTO) throws BusinessException {
        DeviceDTO deviceDTO = deviceService.query(rFileDTO.getDeviceId());
        Storage storage = storageService.getInstance(deviceDTO);
        return storage.downLoad(rFileDTO.getPath());
    }

    @Override
    public void delete(RFileDTO rFileDTO) throws BusinessException {
        DeviceDTO deviceDTO = deviceService.query(rFileDTO.getDeviceId());
        Storage storage = storageService.getInstance(deviceDTO);
        storage.delete(rFileDTO.getPath());
    }

    // 初始化
    private static void initAvatar() {
        File dir = new File("avatars");
        if (!dir.exists()) dir.mkdirs();
        File source = new File("assets/default.png");
        File dest = new File("avatars/default.png");
        if (!dest.exists())
            try {
                Files.copy(source.toPath(), dest.toPath());
            } catch (Exception e) {
                throw new BusinessException(CommonErrorCode.E_500007);
            }
    }

    // 获取文件后缀
    private static String getFileExtension(MultipartFile file) {
        // 获取文件名
        String fileName = file.getOriginalFilename();

        // 获取文件后缀
        if (fileName != null && fileName.lastIndexOf(".") != -1) {
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } else {
            return ""; // 没有后缀名
        }
    }

    // 寻找头像文件
    private static String findAvatar(Long id) {
        List<String> imageTypes = Stream.of("png", "jpg").collect(toList());

        // 构建文件对象
        File folder = new File("avatars");

        // 遍历文件夹中的文件
        File[] files = folder.listFiles();
        if (files != null)
            for (File file : files)
                for (String type: imageTypes)
                    if (file.isFile() && (file.getName().equalsIgnoreCase(String.format("%s.%s", id, type))))
                        return file.getName();


        // 如果没有找到匹配的文件，则返回默认图片
        return "default.png";
    }

    @Override
    public void uploadAvatar(Long accountId, MultipartFile file) throws BusinessException {
        initAvatar();
        String extension = getFileExtension(file);
        // 头像已存在则删除
        String avatar = findAvatar(accountId);
        if (!avatar.equals("default.jpg"))
            new File("avatars/" + avatar).delete();
        String imageUrl = String.format("%s/avatars/%s.%s", System.getProperty("user.dir"), accountId, extension);
        File imageFile = new File(imageUrl);
        try {
            file.transferTo(imageFile);
        } catch (Exception e) {
            throw new BusinessException(CommonErrorCode.E_500006);
        }
    }



    @Override
    public String queryAvatar(Long accountId) throws BusinessException {
        initAvatar();
        return "avatars/" + findAvatar(accountId);
    }

}
