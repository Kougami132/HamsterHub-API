package com.hamsterhub.service.service;

import com.hamsterhub.service.device.ListFiler;
import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.database.dto.VFileDTO;

import java.io.File;
import java.util.List;

public interface FileStorageService {
    // 载入策略和设备数据，springboot启动时调用一次，当修改策略和设备的数据库数据应当刷新一次
    void loadData();

    ListFiler getListFiler(String root);

    // 判断文件是否存在，虚拟文件系统应当传递hash，真实文件系统应当传递url
    Boolean isExist(String root, String index);

    // 通过url查询文件
    List<VFileDTO> queryFile(String root, String url, UserDTO userDTO);

    // 通过凭据获取文件
    VFileDTO getFile(String root, String index, UserDTO userDTO);

    // 获取parent下的文件列表，当虚拟文件目录系统时应当传递parentId，真实的文件目录系统可用直接传父目录url
    List<VFileDTO> queryDirectory(String root, String parent, UserDTO userDTO, Integer page, Integer limit);

    // 创建一个目录，当虚拟文件目录系统时创建一个记录，真实的文件目录系统应当在对应设备上创建一个文件夹
    VFileDTO makeDirectory(String root, String parent, String name, UserDTO userDTO);

    // 获取文件数量，虚拟文件系统应当传递文件id，真实文件系统应当传递url
    Integer queryFileCount(String root, String index);

    void delete(String root, String index, UserDTO userDTO);

    void rename(String root, String index, String name, UserDTO userDTO);

    void copyTo(String root, String index, String parent, UserDTO userDTO);

    void moveTo(String root, String index, String parent, String name, UserDTO userDTO);

    void uploadBefore(String root, String parent, String name, UserDTO userDTO);

    VFileDTO upload(String root, File file, String parent, String name, Long size, String hash, UserDTO userDTO);

    String getDownloadUrl(String root, String index, UserDTO userDTO, Long preference);

    Long getTotalSize(String root, Integer combineOption);

    Long getUsableSize(String root, Integer combineOption);

    String getQueryUrl(String root, String index, UserDTO userDTO);
}
