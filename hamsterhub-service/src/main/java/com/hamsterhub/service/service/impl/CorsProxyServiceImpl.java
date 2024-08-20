package com.hamsterhub.service.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.Base64Util;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.interceptor.DecompressionInterceptor;
import com.hamsterhub.service.service.CorsProxyService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CorsProxyServiceImpl implements CorsProxyService {

    @Autowired
    private RestTemplate restTemplate;

    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new DecompressionInterceptor())
            .build();

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType FORM = MediaType.get("application/x-www-form-urlencoded; charset=utf-8");

    private static final String BILIBILI_PAGE_LIST = "https://api.bilibili.com/x/player/pagelist?";
    private static final String BILIBILI_DANMA_LIST = "https://api.bilibili.com/x/v1/dm/list.so?";

    private static final String ALI_QUERY_QRCODE = "https://api.nn.ci/alist/ali/qr";
    private static final String ALI_QRCODE_IMAGE = "https://api.nn.ci/qr?size=400&text=";
    private static final String ALI_QRCODE_STATUS = "https://api.nn.ci/alist/ali/ck";

    @Override
    public String toPost(String url, String json) throws BusinessException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (okhttp3.Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toGet(String url) throws BusinessException {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept-Encoding", "gzip,deflate")
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String queryPageListBili(String aid, String bvid) throws BusinessException {
        StringBuilder url = new StringBuilder(BILIBILI_PAGE_LIST);
        if(!StringUtil.isBlank(aid)){
            url.append("aid=").append(aid).append("&");
        }

        if(!StringUtil.isBlank(bvid)){
            url.append("bvid=").append(bvid);
        }

        return this.toGet(url.toString());
    }

    public String queryXmlForBili(String cid) throws BusinessException {
        StringBuilder url = new StringBuilder(BILIBILI_DANMA_LIST);
        url.append("oid=").append(cid);
        return this.toGet(url.toString());
    }

    @Override
    public String queryAliQrCode() throws BusinessException {
        JSONObject response = restTemplate.getForObject(ALI_QUERY_QRCODE, JSONObject.class);
        try {
            JSONObject data = response.getJSONObject("content").getJSONObject("data");
            String text = data.getString("codeContent");
            data.put("imageUrl", ALI_QRCODE_IMAGE + text);
            return data.toJSONString();
        }
        catch (Exception e) {
            throw new BusinessException(CommonErrorCode.E_700005);
        }
    }

    @Override
    public String queryAliQrCodeStatus(String ck, String t) throws BusinessException {
        // body
        JSONObject body = new JSONObject();
        body.put("ck", ck);
        body.put("t", t);

        HttpEntity<JSONObject> entity = new HttpEntity<>(body, null);
        JSONObject response = restTemplate.postForObject(ALI_QRCODE_STATUS, entity, JSONObject.class);
        try {
            JSONObject data = response.getJSONObject("content").getJSONObject("data");
            if (data.containsKey("bizExt")) {
                String bizExt = Base64Util.decode(data.getString("bizExt"));
                JSONObject tokenInfo = JSONObject.parseObject(bizExt).getJSONObject("pds_login_result");
                data.put("pds_login_result", tokenInfo);
            }
            return data.toJSONString();
        }
        catch (Exception e) {
            throw new BusinessException(CommonErrorCode.E_700005);
        }
    }

}



