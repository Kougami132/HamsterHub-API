package com.hamsterhub.service.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.dto.SysConfigDTO;
import com.hamsterhub.service.dto.VFileDTO;

import java.util.List;

public interface SysConfigService {

    void init();

    void set(SysConfigDTO sysConfigDTO) throws BusinessException;

    List<SysConfigDTO> query() throws BusinessException;
}
