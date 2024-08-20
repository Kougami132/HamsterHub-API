package com.hamsterhub.service.service;

import com.alibaba.fastjson.JSONObject;
import com.hamsterhub.common.domain.BusinessException;

import java.io.IOException;

public interface CorsProxyService {
    String toPost(String url, String json) throws BusinessException;

    String toGet(String url) throws BusinessException;

    String queryPageListBili(String aid, String bvid) throws BusinessException;
    public String queryXmlForBili(String cid) throws BusinessException;

    /**
     * 阿里云盘登录二维码
     */
    String queryAliQrCode() throws BusinessException;
    String queryAliQrCodeStatus(String ck, String t) throws BusinessException;
}
