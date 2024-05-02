package com.hamsterhub.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamsterhub.service.dto.SysConfigDTO;
import com.hamsterhub.service.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SystemConfig implements WebMvcConfigurer {

    @Autowired
    private SysConfigService sysConfigService;

    private Map<String,SysConfigDTO> configs = null;

    private String cache = null;

    @PostConstruct
    private void loadData() throws JsonProcessingException {
        List<SysConfigDTO> query = sysConfigService.query();

        Map<String,SysConfigDTO> temp = new HashMap<>();

        for (SysConfigDTO sysConfigDTO : query) {
            temp.put(sysConfigDTO.getKey(), sysConfigDTO);
        }

        this.configs = temp;
        ObjectMapper mapper = new ObjectMapper();
        this.cache = mapper.writeValueAsString(temp);
    }


    public String get(String key){
        return configs.get(key).getValue();
    }

    public String getJson(){
        String temp = "";

        if(this.cache != null){
            temp = this.cache;
        }

        return temp;
    }

    public Map<String,SysConfigDTO> getObj(){
        return this.configs;
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
