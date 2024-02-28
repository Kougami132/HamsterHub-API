package com.hamsterhub.service.impl;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

    private final String prefix = "hamster";
    private final Integer expiry = 7;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void addToken(Long accountId, String token) throws BusinessException {
        String key = this.prefix + ":token:" + accountId;
        redisTemplate.opsForSet().add(key, token);
    }

    @Override
    public Boolean checkToken(Long accountId, String token) throws BusinessException {
        String key = this.prefix + ":token:" + accountId;
        return redisTemplate.opsForSet().isMember(key, token);
    }

    @Override
    public void delToken(Long accountId, String token) throws BusinessException {
        String key = this.prefix + ":token:" + accountId;
        redisTemplate.opsForSet().remove(key, token);
    }

    @Override
    public void delAllToken(Long accountId) throws BusinessException {
        String key = this.prefix + ":token:" + accountId;
        redisTemplate.delete(key);
    }

    @Override
    public Long getFileId(String root, Long accountId, String path) throws BusinessException {
        String key = this.prefix + ":" + root + ":" + accountId + ":" + path;
        String s = redisTemplate.opsForValue().get(key);
        if (s == null) return null;
        return Long.parseLong(s);
    }

    @Override
    public void setFileId(String root, Long accountId, String path, Long fileId) throws BusinessException {
        String key = this.prefix + ":" + root + ":" + accountId + ":" + path;
        redisTemplate.opsForValue().set(key, fileId.toString(), this.expiry, TimeUnit.DAYS);
    }

    @Override
    public void delFileId(String root, Long accountId, String path) throws BusinessException {
        String key = this.prefix + ":" + root + ":" + accountId + ":" + path;
        Set<String> keys = redisTemplate.keys(key + "*");
        redisTemplate.delete(keys);
    }

    @Override
    public void delFileId(String root, Long accountId, Long fileId) throws BusinessException {
        Set<String> keys = redisTemplate.keys(this.prefix + ":" + root + ":" + accountId + ":*");
        for (String key: keys) {
            String value = redisTemplate.opsForValue().get(key);
            if (value != null && value.equals(fileId.toString())) {
                redisTemplate.delete(key);
                return;
            }
        }
    }

    @Override
    public boolean isPathExist(String root, Long accountId, String path) throws BusinessException {
        String key = this.prefix + ":" + root + ":" + accountId + ":" + path;
        return redisTemplate.hasKey(key);
    }

    @Override
    public String getAliSession(Long deviceId) throws BusinessException {
        String key = this.prefix + ":ali:" + deviceId;
        String s = redisTemplate.opsForValue().get(key);
        if (s == null) return null;
        return s;
    }

    @Override
    public void setAliSession(Long deviceId, String data) throws BusinessException {
        String key = this.prefix + ":ali:" + deviceId;
        redisTemplate.opsForValue().set(key, data);
    }

    @Override
    public boolean isAliSessionExist(Long deviceId) throws BusinessException {
        String key = this.prefix + ":ali:" + deviceId;
        return redisTemplate.hasKey(key);
    }
}
