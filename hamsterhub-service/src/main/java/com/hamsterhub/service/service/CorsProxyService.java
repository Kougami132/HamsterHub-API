package com.hamsterhub.service.service;

import java.io.IOException;

public interface CorsProxyService {
    String queryPageListBili(String aid, String bvid) throws IOException;
    public String queryXmlForBili(String cid) throws IOException;
}
