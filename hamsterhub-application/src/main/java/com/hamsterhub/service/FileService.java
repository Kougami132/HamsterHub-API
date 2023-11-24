package com.hamsterhub.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.dto.RFileDTO;
import com.hamsterhub.service.dto.StrategyDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String getHash(MultipartFile file) throws BusinessException;
    RFileDTO upload(MultipartFile file, StrategyDTO strategyDTO) throws BusinessException;
    String download(RFileDTO rFileDTO) throws BusinessException;
}
