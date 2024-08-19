package com.hamsterhub.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.database.dto.RFileDTO;
import com.hamsterhub.database.dto.StrategyDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileService {
    String getHash(File file) throws BusinessException;
    RFileDTO upload(File file, StrategyDTO strategyDTO) throws BusinessException;
    String download(RFileDTO rFileDTO) throws BusinessException;
    void delete(RFileDTO rFileDTO) throws BusinessException;
    void uploadAvatar(Long accountId, MultipartFile file) throws BusinessException;
    String queryAvatar(Long accountId) throws BusinessException;
}
