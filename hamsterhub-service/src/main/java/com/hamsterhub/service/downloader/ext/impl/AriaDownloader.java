package com.hamsterhub.service.downloader.ext.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.util.GetBeanUtil;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.downloader.ext.Downloader;
import com.hamsterhub.service.downloader.ext.DownloaderTask;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Slf4j
public class AriaDownloader implements Downloader {
    private String name;
    private Integer type = 2;

    private String address; // 应当是完整的地址，包含 /jsonrpc
    private String token;


    private Integer available = 3;// 标记最大可以同时进行的任务
    private boolean ready = false; // 标记是否就绪状态
    private RestTemplate restTemplate;
    private final OkHttpClient client = new OkHttpClient();
    private String downloadPath = "";
    private JSONObject ariaOption = null;

    private String ARIA_ADD_URL = "aria2.addUri";
    private String ARIA_REMOVE = "aria2.remove";
    private String ARIA_REMOVE_FORCE = "aria2.forceRemove";
    private String ARIA_REMOVE_DOWNLOAD_RESULT = "aria2.removeDownloadResult";
    private String ARIA_TELL_STATUS = "aria2.tellStatus";
    private String ARIA_TELL_ACTIVE = "aria2.tellActive";
    private String ARIA_TELL_WAITING = "aria2.tellWaiting";
    private String ARIA_TELL_STOPPED = "aria2.tellStopped";
    private String ARIA_GET_GLOBAL_OPTION = "aria2.getGlobalOption";

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Integer getType() {
        return this.type;
    }

    public AriaDownloader(String address, String token){
        restTemplate = GetBeanUtil.getBean(RestTemplate.class);
        this.address = address;
        this.token = token;
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
    public Boolean connect() throws BusinessException {
        try {
            // 连接Aria2，检查是否可用
            this.ariaOption = getGlobalOption();
            if (this.ariaOption == null) {
                this.ready = false;
                return false;
            }

            this.downloadPath = this.ariaOption.getString("dir");

        } catch (Exception e) {
            log.error("Aria2连接错误，连接地址：{}", this.address);
            return false;
        }

        return true;
    }

    private String sendAriaMsg(String method,Object[] params){
        // 构造请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("jsonrpc", "2.0");
        requestBody.put("method", method);
        requestBody.put("id", "1");
        requestBody.put("params", params);

        String json = JSON.toJSONString(requestBody);

        // 创建Request对象
        RequestBody body = RequestBody.create(json, okhttp3.MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(this.address)
                .post(body)
                .build();

        // 执行请求并获取响应
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // 解析响应
            String responseBody = response.body().string();
//            JSONObject resultMap = JSON.parseObject(responseBody);

            // 返回种子的GID
//            String gid = resultMap.getString("result");
            return responseBody;
        } catch (IOException e) {
            return null;
//            throw new RuntimeException(e);
        }

    }

    private JSONObject getGlobalOption() {
        String res = sendAriaMsg(ARIA_GET_GLOBAL_OPTION, new Object[]{"token:" + this.token});

        if (StringUtil.isBlank(res)){
            return null;
        }

        JSONObject resultMap = JSON.parseObject(res);
        return resultMap.getJSONObject("result");
    }

    private JSONArray getDownloaderList(String method) {
        String res = sendAriaMsg(
                method,
                new Object[]{
                        "token:" + this.token,
                        0,1000,
                        new Object[]{"gid","following","followedBy","completedLength","infoHash","status","totalLength"}
                }
        );

        if (StringUtil.isBlank(res)){
            return null;
        }

        JSONObject resultMap = JSON.parseObject(res);
        return resultMap.getJSONArray("result");
    }

    private JSONObject getStatus(String gid) {
        String res = sendAriaMsg(
                ARIA_TELL_STATUS,
                new Object[]{
                        "token:" + this.token,
                        gid,
                        new Object[]{"gid","following","followedBy","completedLength","infoHash","status","totalLength"}
                }
        );

        if (StringUtil.isBlank(res)){
            return null;
        }

        JSONObject resultMap = JSON.parseObject(res);
        return resultMap.getJSONObject("result");
    }

    private void removeAll(String gid) {

        JSONObject status = getStatus(gid);
        JSONArray followedBy = status.getJSONArray("followedBy");
        if (followedBy != null) {
            for (int i = 0; i < followedBy.size(); i++) {
                String followedByGid = followedBy.getString(i);
                this.removeAll(followedByGid);
            }
        }

        //删除所有记录，包括完成的和下载中的
        sendAriaMsg(
                ARIA_REMOVE_FORCE,
                new Object[]{
                        "token:" + this.token,
                        gid
                }
        );

        sendAriaMsg(
                ARIA_REMOVE_DOWNLOAD_RESULT,
                new Object[]{
                        "token:" + this.token,
                        gid
                }
        );
    }

    private DownloaderTask createDownloaderTask(JSONObject jsonObj) {
        DownloaderTask task = new DownloaderTask();
        task.setName(jsonObj.getString("gid"));
        task.setCompleted(jsonObj.getLong("completedLength"));
        task.setTotal_size(jsonObj.getLong("totalLength"));
        task.setTaskIndex(jsonObj.getString("gid"));
        String status = jsonObj.getString("status");

        if("error".equals(status) || "removed".equals(status)){
            task.setState("error");
        }else {
            task.setState("");
        }

        return task;
    }

    private void tasksProcess(Set<String> visited ,List<DownloaderTask> tasks,JSONArray activeList){
        for (int i = 0; i < activeList.size(); i++) {
            JSONObject activeObj = activeList.getJSONObject(i);
            String gid = activeObj.getString("gid");

            if (visited.contains(gid)) {
                continue;
            }

            // 衍生任务不考虑
            String following = activeObj.getString("following");
            if (StringUtil.isNotBlank(following)){
                continue;
            }

            visited.add(gid);

            // 存在衍生任务则使用衍生任务的数据
            JSONArray followedBy = activeObj.getJSONArray("followedBy");

            if (followedBy!=null && !followedBy.isEmpty()){
                JSONObject followedStatus = this.getStatus(followedBy.getString(0));
                if (followedStatus != null){
                    activeObj = followedStatus;
                }
            }

            visited.add(activeObj.getString("gid"));
            DownloaderTask downloaderTask = createDownloaderTask(activeObj);
            downloaderTask.setTaskIndex(gid);
            tasks.add(downloaderTask);
        }

    }

    @Override
    public List<DownloaderTask> getAllTask() throws BusinessException {
        Set<String> visited = new HashSet<>();
        List<DownloaderTask> tasks = new ArrayList<>();
        JSONArray downloaderList = this.getDownloaderList(ARIA_TELL_ACTIVE);
        if (downloaderList != null){
            this.tasksProcess(visited, tasks, downloaderList);
        }

        downloaderList = this.getDownloaderList(ARIA_TELL_WAITING);
        if (downloaderList != null){
            this.tasksProcess(visited, tasks, downloaderList);
        }

        downloaderList = this.getDownloaderList(ARIA_TELL_STOPPED);
        if (downloaderList != null){
            this.tasksProcess(visited, tasks, downloaderList);
        }

        return tasks;
    }

    @Override
    public String addTask(String tag,String url) throws BusinessException {
        Map<String, String> options = new HashMap<>();
        options.put("dir",this.downloadPath+"/"+tag+"/");
        options.put("referer","*");
        String res = sendAriaMsg(
                ARIA_ADD_URL,
                new Object[]{
                    "token:" + this.token,
                    new Object[]{url},
                    options
                }
        );

        if (StringUtil.isBlank(res)){
            return "";
        }
        JSONObject resultMap = JSON.parseObject(res);

        return resultMap.getString("result");
    }

    @Override
    public Boolean deleteTask(String index) throws BusinessException {
        try {
            this.removeAll(index);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    @Override
    public DownloaderTask getTask(String index) throws BusinessException {

        if (StringUtil.isBlank(index)){
            return null;
        }


        JSONObject status = this.getStatus(index);
        String gid = status.getString("gid");

        if (status == null){
            return null;
        }

        JSONArray followedBy = status.getJSONArray("followedBy");
        if (followedBy!=null && !followedBy.isEmpty()){
            JSONObject followedStatus = this.getStatus(followedBy.getString(0));
            if (followedStatus != null){
                status = followedStatus;
            }
        }

        DownloaderTask downloaderTask = createDownloaderTask(status);
        downloaderTask.setTaskIndex(gid);
        return downloaderTask;
    }

    @Override
    public boolean filter(String fileName){
        if (StringUtil.isBlank(fileName)){
            return false;
        }else if (fileName.endsWith(".aria2") || "get".equals(fileName)){ // 过滤aria2产生的文件
            return false;
        }

        return true;
    }

}
