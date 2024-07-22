package com.hamsterhub.service.service;

import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.dto.ShareDTO;
import com.hamsterhub.service.dto.VFileDTO;

import java.util.List;

public interface ShareFileStorageService {
    ShareDTO shareFile(String root, String index, AccountDTO accountDTO, String key, Long expiry, String name);

    Boolean deleteShare(String root, Long shareId, AccountDTO accountDTO);

    VFileDTO queryShareFile(String ticket, String key, String vFileId);

    VFileDTO searchShareFile(String ticket, String key, String parentIndex, String name);

    List<VFileDTO> queryList(String ticket, String key, String parentIndex, Integer page, Integer limit);

    String downloadShare(String ticket, String key, String index, Long preference);
}
