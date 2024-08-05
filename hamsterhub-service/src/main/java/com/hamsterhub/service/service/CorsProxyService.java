package com.hamsterhub.service.service;

import java.io.IOException;

public interface CorsProxyService {
    String toPost(String url, String json) throws IOException;

    String toGet(String url) throws IOException;

    String queryPageListBili(String aid, String bvid) throws IOException;
    public String queryXmlForBili(String cid) throws IOException;
}
