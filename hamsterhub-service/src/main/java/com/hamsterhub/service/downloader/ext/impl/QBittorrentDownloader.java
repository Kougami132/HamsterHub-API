package com.hamsterhub.service.downloader.ext.impl;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.util.GetBeanUtil;
import com.hamsterhub.service.downloader.ext.Downloader;
import com.hamsterhub.service.downloader.ext.DownloaderTask;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
public class QBittorrentDownloader implements Downloader {
    private String name;
    private Integer type = 1;

    private String address;
    private String username;
    private String password;


    private Integer available = 3;// 标记最大可以同时进行的任务

    private boolean ready = false; // 标记是否就绪状态

    private RestTemplate restTemplate;

    private String cookie;

    private final OkHttpClient client = new OkHttpClient();

    public QBittorrentDownloader(String address,String username,String password){
        restTemplate = GetBeanUtil.getBean(RestTemplate.class);
        this.address = address;
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public void setReady(boolean ready) {
        this.ready = ready;
    }

    @Override
    public Integer getAvailable() {
        return available;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getType() {
        return type;
    }

    @Override
    public Boolean connect() throws BusinessException {
        String url = this.address + "/api/v2/auth/login";

        // header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // form
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("username", username);
        form.add("password", password);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful())
                return false;

            this.cookie = response.getHeaders().getFirst("set-cookie");
            return true;
        }
        catch (Exception e) {
            log.error("qBittorrent连接错误，连接地址：{}", url);
            return false;
        }
    }

    @Override
    public List<DownloaderTask> getAllTask() throws BusinessException {
        String url = this.address + "/api/v2/torrents/info";

        HttpHeaders headers = new HttpHeaders();
        headers.set("cookie", this.cookie);

        ParameterizedTypeReference<List<DownloaderTask>> typeRef = new ParameterizedTypeReference<List<DownloaderTask>>() {};
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        ResponseEntity<List<DownloaderTask>> response = restTemplate.exchange(url, HttpMethod.POST, entity, typeRef);
        List<DownloaderTask> res = response.getBody();
        for (DownloaderTask task : res)
            task.setTaskIndex(task.getTags());
        return res;
    }

    @Override
    public String addTask(String tag, String magnet) throws BusinessException {
        String url = this.address + "/api/v2/torrents/add";

        // header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("cookie", this.cookie);

        // form
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("tags", tag);
        form.add("urls", magnet);
        form.add("savepath", tag);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful())
                return "";
            if (response.getBody().equals("Ok."))
                return tag;
        }
        catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        return "";
    }

    public String addTask(String tag, File file) throws BusinessException {
        String url = this.address + "/api/v2/torrents/add";

        // 创建请求体
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("tags", tag)
                .addFormDataPart("savepath", tag)
                .addFormDataPart("torrents", file.getName(),
                        RequestBody.create(file, okhttp3.MediaType.parse("application/x-bittorrent")))
                .build();

        // 创建请求
        Request request = new Request.Builder()
                .url(url)
                .addHeader("cookie", this.cookie)
                .post(requestBody)
                .build();

        // 执行请求
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            String responseBody = response.body().string();

            if ("Ok.".equals(responseBody.trim()))
                return tag;
            else
                return "";

        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("Failed to add torrent task", e);
        }
    }

    @Override
    public Boolean deleteTask(String tag) throws BusinessException {
        DownloaderTask torrent = getTask(tag);
        if (torrent == null) return false;
        String hash = torrent.getHash();

        String url = this.address + "/api/v2/torrents/delete";

        // header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("cookie", this.cookie);

        // form
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("hashes", hash);
        form.add("deleteFiles", "true");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful())
                return false;
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public DownloaderTask getTask(String tag) throws BusinessException {
        List<DownloaderTask> downloaderTasks = getAllTask();
        for (DownloaderTask i: downloaderTasks)
            if (i.getTags().equals(tag))
                return i;
        return null;
    }

    @Override
    public boolean filter(String fileName) {
        return true;
    }

}
