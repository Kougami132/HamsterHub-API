package com.hamsterhub.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamsterhub.convert.SysConfigConvert;
import com.hamsterhub.initialize.DatabaseInitialize;
import com.hamsterhub.response.SysConfigResponse;
import com.hamsterhub.service.dto.SysConfigDTO;
import com.hamsterhub.service.service.SysConfigService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class SystemConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(SystemConfig.class);
    @Autowired
    private SysConfigService sysConfigService;

    @Autowired // 调整PostConstruct的执行顺序
    private DatabaseInitialize databaseInitialize;

    private Map<String,SysConfigDTO> configs = null;

    private Map<String,SysConfigResponse> cache = null;

    @Getter
    private String cacheId = "";

    @PostConstruct
    private void loadData() throws JsonProcessingException {
        List<SysConfigDTO> query = sysConfigService.query();

        Map<String,SysConfigDTO> temp = new HashMap<>();
        Map<String, SysConfigResponse> _cache = new HashMap<>();

        for (SysConfigDTO sysConfigDTO : query) {
            temp.put(sysConfigDTO.getKey(), sysConfigDTO);
            if(!sysConfigDTO.getHide()){
                _cache.put(sysConfigDTO.getKey(), SysConfigConvert.INSTANCE.dto2res(sysConfigDTO));
            }
        }
        this.cacheId = UUID.randomUUID().toString();

        // 添加缓存标记
        temp.put("hash",new SysConfigDTO("hash",this.cacheId));
        _cache.put("hash",new SysConfigResponse("hash",this.cacheId));

        this.configs = temp;
        this.cache = _cache;

    }


    public String get(String key){
        return configs.get(key).getValue();
    }

    public Map<String,SysConfigResponse> getObj(){
        return this.cache;
    }


    public void set(String key, String value){
        sysConfigService.set(new SysConfigDTO(key,value));

        try {
            this.loadData();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}
