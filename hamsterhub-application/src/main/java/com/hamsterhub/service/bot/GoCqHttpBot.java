package com.hamsterhub.service.bot;

import com.alibaba.fastjson.JSONObject;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.domain.ConfigKey;
import com.hamsterhub.config.SystemConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class GoCqHttpBot {

    private String base_url;

    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {
        base_url = systemConfig.get(ConfigKey.BOT_GOCQ_URL);
    }

    public void setBaseUrl(String url) throws BusinessException {
        if (url.charAt(url.length() - 1) == '/')
            url = url.substring(0, url.length() - 1);

        // 测试url,无效不保存
        base_url = url;
        if (verify())
            systemConfig.set(ConfigKey.BOT_GOCQ_URL, url);
        else
            this.init();
    }

    public Boolean verify() {
        try {
            Long qq = this.getBotQQ();
            return qq > 0;
        }
        catch (Exception e) {
            return false;
        }
    }

    public Long getBotQQ() throws BusinessException {
        String url = base_url + "/get_login_info";

        try {
            ResponseEntity<JSONObject> response = restTemplate.getForEntity(url, JSONObject.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                JSONObject body = response.getBody();
                if (body != null) {
                    JSONObject data = body.getJSONObject("data");
                    if (data != null)
                        return data.getLong("user_id");
                }
                log.error("获取机器人QQ失败: {}", body);
            }
        } catch (Exception e) {
            log.error("获取机器人QQ失败: {}", e.getMessage());
        }
        throw new BusinessException(CommonErrorCode.E_700004);
    }

    public void pushMsg(Boolean isGroup, Long targetId, String message) throws BusinessException {
        String url = base_url + "/send_msg";

        // header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        String type = isGroup ? "group" : "private";
        body.add("message_type", type);
        body.add(isGroup ? "group_id" : "user_id", targetId);
        body.add("message", message);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful())
                log.info("消息推送成功: {}, {}, {}, {}", type, targetId, message, response.getBody());
            else {
                log.error("消息推送失败: {}, {}", response.getStatusCode(), response.getBody());
                throw new BusinessException(CommonErrorCode.E_700004);
            }
        }
        catch (Exception e) {
            log.error("消息推送失败: {}", e.getMessage());
            throw new BusinessException(CommonErrorCode.E_700004);
        }
    }
}
