package com.hamsterhub.service.impl;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.util.JwtUtil;
import com.hamsterhub.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

    private final String prefix = "hamster";
    private final Integer expiry = 7;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void addTokenBlacklist(String token) throws BusinessException {
        String signature = token.substring(token.lastIndexOf('.') + 1);
        String key = this.prefix + ":jwt:blacklist:" + signature;
        redisTemplate.opsForValue().set(key, "1", Duration.between(LocalDateTime.now(), JwtUtil.getExpiryTime(token)).toMinutes(), TimeUnit.MINUTES);
    }

    @Override
    public Boolean checkToken(String token) throws BusinessException {
        String signature = token.substring(token.lastIndexOf('.') + 1);
        String key = this.prefix + ":jwt:blacklist:" + signature;
        return redisTemplate.hasKey(key);
    }

    @Override
    public void setPhoneCode(Long phone, String code) throws BusinessException {
        String key = this.prefix + ":code:phone:" + phone;
        redisTemplate.opsForValue().set(key, code, 2, TimeUnit.MINUTES);
    }

    @Override
    public String getPhoneCode(Long phone) throws BusinessException {
        String key = this.prefix + ":code:phone:" + phone;
        String s = redisTemplate.opsForValue().get(key);
        if (s == null) return null;
        return s;
    }

    @Override
    public void phoneCount(Long phone) throws BusinessException {
        String key = this.prefix + ":limit:phone:" + phone;
        String s = redisTemplate.opsForValue().get(key);
        int count = 0;
        if (s != null) count = Integer.valueOf(s);
        redisTemplate.opsForValue().set(key, String.valueOf(count + 1), getRemainSecondsOneDay(), TimeUnit.SECONDS);
    }

    public static long getRemainSecondsOneDay() {
        long cur = System.currentTimeMillis() / 1000;
        int hour = 60 * 60;
        cur += hour * 8;
        cur %= hour * 24;
        return hour * 24 - cur;
    }

    @Override
    public boolean isPhoneLimited(Long phone) throws BusinessException {
        String key = this.prefix + ":limit:phone:" + phone;
        String s = redisTemplate.opsForValue().get(key);
        int count = 0;
        if (s != null) count = Integer.valueOf(s);
        return count >= 3;
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
                Set<String> deleteKeys = redisTemplate.keys(key + "*");
                redisTemplate.delete(deleteKeys);
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
