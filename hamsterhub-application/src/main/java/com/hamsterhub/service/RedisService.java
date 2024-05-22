package com.hamsterhub.service;

import com.hamsterhub.common.domain.BusinessException;

import java.time.LocalDateTime;

public interface RedisService {
    /**
     * JWT黑名单
     */
    void addTokenBlacklist(String token) throws BusinessException;
    Boolean checkToken(String token) throws BusinessException;

    /**
     * 手机验证码
     */
    void setPhoneCode(Long phone, String code) throws BusinessException;
    String getPhoneCode(Long phone) throws BusinessException;
    void phoneCount(Long phone) throws BusinessException;
    boolean isPhoneLimited(Long phone) throws BusinessException;

    /**
     * 路径缓存
     */
    Long getFileId(String root, Long accountId, String path) throws BusinessException;
    void setFileId(String root, Long accountId, String path, Long fileId) throws BusinessException;
    void delFileId(String root, Long accountId, String path) throws BusinessException;
    void delFileId(String root, Long accountId, Long fileId) throws BusinessException;
    boolean isPathExist(String root, Long accountId, String path) throws BusinessException;

    /**
     * 阿里云盘token
     */
    String getAliSession(Long deviceId) throws BusinessException;
    void setAliSession(Long deviceId, String data) throws BusinessException;
    boolean isAliSessionExist(Long deviceId) throws BusinessException;

    /**
     * 下载任务
     */

}
