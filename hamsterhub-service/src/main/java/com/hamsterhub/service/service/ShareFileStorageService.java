package com.hamsterhub.service.service;

import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.database.dto.ShareDTO;
import com.hamsterhub.database.dto.VFileDTO;

import java.util.List;

public interface ShareFileStorageService {
    ShareDTO shareFile(String root, String index, UserDTO userDTO, String key, Long expiry, String name);

    Boolean deleteShare(String root, Long shareId, UserDTO userDTO);

    VFileDTO queryShareFile(String ticket, String key, String vFileId);

    VFileDTO searchShareFile(String ticket, String key, String parentIndex, String name);

    List<VFileDTO> queryList(String ticket, String key, String parentIndex, Integer page, Integer limit);

    String downloadShare(String ticket, String key, String index, Long preference);
}
