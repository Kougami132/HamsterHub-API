package com.hamsterhub.service.device;

import com.hamsterhub.service.dto.VFileDTO;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface ListFiler {

    // 查询是否就绪
    public Boolean isReady();

    Integer getFileSystem();

    // 查询文件是否存在，虚拟文件系统应当传递hash，真实文件系统应当传递url
    Boolean isExist(String index);

    List<VFileDTO> queryFile(String url, Long userId);

    VFileDTO getFile(String index, Long userId);

    // 查询目录下的文件
    public List<VFileDTO> queryDirectory(String parentId, Long userId, Integer page, Integer limit);

    VFileDTO makeDirectory(String parent, String name, Long userId);

    Integer queryFileCount(String index);

    void delete(String index, Long userId);

    void rename(String index, String name, Long userId);

    void copyTo(String index, String parent, Long userId);

    void moveTo(String index, String parent, String name, Long userId);

    void uploadBefore(String parent, String name, Long userId);

    VFileDTO upload(String parent, File file, String name, Long size, Long userId, String hash);

    String getDownloadUrl(String index, Long userId, Long preference);

    Integer getCombineNumber();

    Long getTotalSize(Integer combineOption);

    Long getUsableSize(Integer combineOption);

    String getQueryUrl(String index, Long userId);
}
