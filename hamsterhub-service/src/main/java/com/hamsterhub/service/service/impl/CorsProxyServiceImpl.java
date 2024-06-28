package com.hamsterhub.service.service.impl;

import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.service.CorsProxyService;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

@Service
public class CorsProxyServiceImpl implements CorsProxyService {

    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new DecompressionInterceptor())
            .build();

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType FORM = MediaType.get("application/x-www-form-urlencoded; charset=utf-8");

    private static final String BILIBILI_PAGE_LIST = "https://api.bilibili.com/x/player/pagelist?";
    private static final String BILIBILI_DANMA_LIST = "https://api.bilibili.com/x/v1/dm/list.so?";

    private String toPost(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (okhttp3.Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private String toGet(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .header("Accept-Encoding", "gzip,deflate")
                .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public String queryPageListBili(String aid, String bvid) throws IOException {
        StringBuilder url = new StringBuilder(BILIBILI_PAGE_LIST);
        if(!StringUtil.isBlank(aid)){
            url.append("aid=").append(aid).append("&");
        }

        if(!StringUtil.isBlank(bvid)){
            url.append("bvid=").append(bvid);
        }

        return this.toGet(url.toString());
    }

    public String queryXmlForBili(String cid) throws IOException {
        StringBuilder url = new StringBuilder(BILIBILI_DANMA_LIST);
        url.append("oid=").append(cid);
        return this.toGet(url.toString());
    }

}

// 解压请求内容拦截器
class DecompressionInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);

        String contentEncoding = response.header("Content-Encoding");

        if (contentEncoding == null || response.body() == null) {
            return response;
        }

        ResponseBody responseBody;
        if ("gzip".equalsIgnoreCase(contentEncoding)) {
            responseBody = ResponseBody.create(response.body().contentType(), decompressGzip(response.body().byteStream()));
        } else if ("deflate".equalsIgnoreCase(contentEncoding)) {
            responseBody = ResponseBody.create(response.body().contentType(), decompressDeflate(response.body().byteStream()));
        } else {
            return response;
        }

        return response.newBuilder()
                .body(responseBody)
                .removeHeader("Content-Encoding")
                .build();
    }

    private byte[] decompressGzip(java.io.InputStream inputStream) throws IOException {
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
             InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    private byte[] decompressDeflate(java.io.InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Inflater inflater = new Inflater(true);
        try (InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream, inflater)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inflaterInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        } catch (Exception e) {
            inflater.end();
            throw e;
        }
        return outputStream.toByteArray();
    }
}

