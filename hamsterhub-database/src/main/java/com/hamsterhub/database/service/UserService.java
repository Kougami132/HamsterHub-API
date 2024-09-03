package com.hamsterhub.database.service;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.database.dto.UserDTO;

import java.util.List;

public interface UserService {
    void init() throws BusinessException;
    UserDTO create(UserDTO userDTO) throws BusinessException;
    void delete(Long userId) throws BusinessException;
    void update(UserDTO userDTO) throws BusinessException;

    void updateForAdmin(UserDTO userDTO) throws BusinessException;

    UserDTO query(Long userId) throws BusinessException;
    UserDTO query(String username) throws BusinessException;
    Boolean isExist(Long userId) throws BusinessException;
    Boolean isExist(String username) throws BusinessException;
    Boolean isPhoneExist(Long phone) throws BusinessException;

    List<UserDTO> FetchAll() throws BusinessException;
}
