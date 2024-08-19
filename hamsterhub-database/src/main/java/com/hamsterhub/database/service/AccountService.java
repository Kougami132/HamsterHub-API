package com.hamsterhub.database.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.database.dto.AccountDTO;

import java.util.List;

public interface AccountService {
    void init() throws BusinessException;
    AccountDTO create(AccountDTO accountDTO) throws BusinessException;
    void delete(Long accountId) throws BusinessException;
    void update(AccountDTO accountDTO) throws BusinessException;

    void updateForAdmin(AccountDTO accountDTO) throws BusinessException;

    AccountDTO query(Long accountId) throws BusinessException;
    AccountDTO query(String username) throws BusinessException;
    Boolean isExist(Long accountId) throws BusinessException;
    Boolean isExist(String username) throws BusinessException;
    Boolean isPhoneExist(Long phone) throws BusinessException;

    List<AccountDTO> FetchAll() throws BusinessException;
}
