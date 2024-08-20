package com.hamsterhub.service.interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

// 解压请求内容拦截器
public class DecompressionInterceptor implements Interceptor {
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