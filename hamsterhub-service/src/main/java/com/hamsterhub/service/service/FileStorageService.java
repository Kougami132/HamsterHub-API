package com.hamsterhub.service.service;

import com.hamsterhub.service.device.ListFiler;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.dto.VFileDTO;

import java.io.File;
import java.util.List;

public interface FileStorageService {
    // 载入策略和设备数据，springboot启动时调用一次，当修改策略和设备的数据库数据应当刷新一次
    void loadData();

    ListFiler getListFiler(String root);

    // 判断文件是否存在，虚拟文件系统应当传递hash，真实文件系统应当传递url
    Boolean isExist(String root, String index);

    // 通过url查询文件
    List<VFileDTO> queryFile(String root, String url, AccountDTO accountDTO);

    // 获取parent下的文件列表，当虚拟文件目录系统时应当传递parentId，真实的文件目录系统可用直接传父目录url
    List<VFileDTO> queryDirectory(String root, String parent, AccountDTO accountDTO, Integer page, Integer limit);

    // 创建一个目录，当虚拟文件目录系统时创建一个记录，真实的文件目录系统应当在对应设备上创建一个文件夹
    VFileDTO makeDirectory(String root, String parent, String name, AccountDTO accountDTO);

    // 获取文件数量，虚拟文件系统应当传递文件id，真实文件系统应当传递url
    Integer queryFileCount(String root, String index);

    void delete(String root, String index, AccountDTO accountDTO);

    void rename(String root, String index, String name, AccountDTO accountDTO);

    void copyTo(String root, String index, String parent, AccountDTO accountDTO);

    void moveTo(String root, String index, String parent, AccountDTO accountDTO);

    void uploadBefore(String root, String parent, String name, AccountDTO accountDTO);

    VFileDTO upload(String root, File file, String parent, String name, Long size, String hash, AccountDTO accountDTO);

    String getDownloadUrl(String root, String index, AccountDTO accountDTO, Long preference);

    Long getTotalSize(String root, Integer combineOption);

    Long getUsableSize(String root, Integer combineOption);
}
