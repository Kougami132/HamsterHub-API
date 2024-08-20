package com.hamsterhub.service.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hamsterhub.common.domain.ConfigKey;
import com.hamsterhub.common.util.JwtUtil;
import com.hamsterhub.service.convert.SysConfigConvert;
import com.hamsterhub.service.entity.SysConfigResponse;
import com.hamsterhub.database.dto.SysConfigDTO;
import com.hamsterhub.database.service.SysConfigService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@DependsOn("databaseInitialize") // 调整PostConstruct的执行顺序
public class SystemConfig {

    @Autowired
    private SysConfigService sysConfigService;

    private Map<String,SysConfigDTO> configs = null;

    private Map<String,SysConfigResponse> cache = null;

    @Getter
    private String cacheId = "";

    @PostConstruct
    private void loadData() throws JsonProcessingException {
        List<SysConfigDTO> query = sysConfigService.query();

        Map<String, SysConfigDTO> temp = new HashMap<>();
        Map<String, SysConfigResponse> _cache = new HashMap<>();

        for (SysConfigDTO sysConfigDTO : query) {
            temp.put(sysConfigDTO.getKey(), sysConfigDTO);
            if (!sysConfigDTO.getHide()) {
                _cache.put(sysConfigDTO.getKey(), SysConfigConvert.INSTANCE.dto2res(sysConfigDTO));
            }
        }
        this.cacheId = UUID.randomUUID().toString();

        // 添加缓存标记
        temp.put("hash", new SysConfigDTO("hash", this.cacheId));
        _cache.put("hash", new SysConfigResponse("hash", this.cacheId));

        this.configs = temp;
        this.cache = _cache;

        JwtUtil.setSecretKey(get(ConfigKey.JWT_SECRET_KEY));
    }

    public Map<String, SysConfigResponse> getObj(){
        return this.cache;
    }

    public String get(String key){
        return configs.get(key).getValue();
    }

    public SysConfigDTO getForSetting(String key){
        return configs.get(key);
    }

    public void set(String key, String value){
        sysConfigService.set(new SysConfigDTO(key,value));

        try {
            this.loadData();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    // 判断某个配置是不是为true
    public Boolean check(String key){
        return ConfigKey.isTrue(this.get(key));
    }

}
