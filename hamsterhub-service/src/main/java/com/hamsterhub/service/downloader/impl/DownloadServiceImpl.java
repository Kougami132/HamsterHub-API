package com.hamsterhub.service.downloader.impl;


import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.database.dto.AccountDTO;
import com.hamsterhub.database.dto.DownloadTaskDTO;
import com.hamsterhub.database.dto.VFileDTO;
import com.hamsterhub.service.device.ListFiler;
import com.hamsterhub.database.enums.DownloadOrigin;
import com.hamsterhub.service.downloader.DownloadService;
import com.hamsterhub.database.enums.DownloadState;
import com.hamsterhub.database.enums.DownloadType;
import com.hamsterhub.service.downloader.ext.Downloader;
import com.hamsterhub.service.downloader.ext.DownloaderTask;
import com.hamsterhub.service.downloader.ext.impl.AriaDownloader;
import com.hamsterhub.service.downloader.ext.impl.QBittorrentDownloader;
import com.hamsterhub.service.entity.DownloaderOption;
import com.hamsterhub.database.service.DownloadTaskService;
import com.hamsterhub.service.service.FileStorageService;
import com.hamsterhub.database.service.RSSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;

@Slf4j
@Service
public class DownloadServiceImpl implements DownloadService {
    @Value("${bit-torrent.address}")
    private String qBitAddress;
    @Value("${bit-torrent.username}")
    private String qBitUsername;
    @Value("${bit-torrent.password}")
    private String qBitPassword;


    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private DownloadTaskService downloadTaskService;

    @Autowired
    private RSSService rssService;

    private final Map<Integer, Downloader> downloaders = new HashMap<>();

    @Override
    @PostConstruct
    public void loadData(){
        downloaders.clear();

        Downloader downloader = new QBittorrentDownloader(qBitAddress, qBitUsername, qBitPassword);
        downloader.setName("QBittorrent");
        downloaders.put(1, downloader);

        downloader = new AriaDownloader("http://localhost:6800/jsonrpc","");
        downloader.setName("Aria2");
        downloaders.put(2, downloader);

        this.renewDownloader();
    }

    @Override
    public Downloader getDownloader(int id){
        Downloader downloader = downloaders.get(id);

        // 防止异常
        CommonErrorCode.checkAndThrow(downloader == null, CommonErrorCode.E_120001);
        CommonErrorCode.checkAndThrow(!downloader.isReady(), CommonErrorCode.E_120002);
        return downloader;
    }

    @Override
    public Map<Integer, Downloader> getAllDownloaders(){
        return downloaders;
    }

    @Override
    public List<DownloaderOption> getDownloaderOption(AccountDTO user){
        List<DownloaderOption> res = new ArrayList<>();

        for (Map.Entry<Integer, Downloader> entry : downloaders.entrySet()) {
            // 不返回未就绪的下载器
            if (!entry.getValue().isReady()){
                continue;
            }

            DownloaderOption downloaderOption = new DownloaderOption();

            downloaderOption.setId(entry.getKey());
            downloaderOption.setName(entry.getValue().getName());
            downloaderOption.setType(entry.getValue().getType());

            res.add(downloaderOption);
        }

        return res;
    }

    @Override
    public String addDownloadTaskForUser(AccountDTO user, Integer downloaderId, String root,
                                         String parent, String url, String name){
        // 获取下载器
        Downloader downloader = getDownloader(downloaderId);

        // 检查父目录
        VFileDTO file = fileStorageService.getFile(root, parent, user);
        CommonErrorCode.checkAndThrow(file == null, CommonErrorCode.E_600001);
        CommonErrorCode.checkAndThrow(!file.isDir(), CommonErrorCode.E_600013);

        String tag = StringUtil.generateRandomString(16);

        DownloadTaskDTO taskDTO = DownloadTaskDTO.createTask(root,parent, DownloadOrigin.USER.ordinal(),
                user.getId(), DownloadType.URL.ordinal(),url ,downloaderId,user.getId(), tag, name);

        downloadTaskService.create(taskDTO);
        return tag;
    }

//    public String addDownloadTaskForRSS(DownloadTaskDTO taskDTO){
//        // 获取下载器
//        Downloader downloader = getDownloader(downloaderId);
//
//        // 检查父目录
//        VFileDTO file = fileStorageService.getFile(root, parent, user);
//        CommonErrorCode.checkAndThrow(file == null, CommonErrorCode.E_600001);
//        CommonErrorCode.checkAndThrow(!file.isDir(), CommonErrorCode.E_600013);
//
//        String tag = StringUtil.generateRandomString(16);
//
//        DownloadTaskDTO taskDTO = DownloadTaskDTO.createTask(root,parent, DownloadOrigin.USER.ordinal(),
//                user.getId(), DownloadType.URL.ordinal(),url ,downloaderId,user.getId(), tag);
//
//        downloadTaskService.create(taskDTO);
//        return tag;
//    }

    @Override
    public List<DownloadTaskDTO> getList(AccountDTO user){
        List<DownloadTaskDTO> downloadTaskDTOS = downloadTaskService.fetchByUser(user.getId());

        for (DownloadTaskDTO taskDTO : downloadTaskDTOS){
            if (taskDTO.getState().equals(0)){
                // 等于0就不用获取信息了
                break;
            }

            if (taskDTO.getState().equals(DownloadState.DOWNLOADING.ordinal())){
                DownloaderTask downloaderTask = null;
                try {
                    // 将有进度的部分存入
                    Downloader downloader = getDownloader(taskDTO.getDownloader());
                    downloaderTask = downloader.getTask(taskDTO.getTaskIndex());
                } catch (Exception e) {
                    continue;
                }

                if (downloaderTask != null){
                    taskDTO.setCompleted(downloaderTask.getCompleted());
                    taskDTO.setTotal(downloaderTask.getTotal_size());
                }
            }
        }

        return downloadTaskDTOS;
    }

    @Override
    public void deleteDownloadTask(Integer downloaderId, String tag, AccountDTO user){
        Downloader downloader = getDownloader(downloaderId);
        DownloadTaskDTO TaskDTO = downloadTaskService.query(user.getId(), tag);

        // 防止越权
        CommonErrorCode.checkAndThrow(TaskDTO == null, CommonErrorCode.E_100007);

        Boolean result = downloader.deleteTask(TaskDTO.getTaskIndex());

        downloadTaskService.delete(tag);

        // 同时删除文件
        String savePath = "temp/downloads/" + tag;
        File dir = new File(savePath);
        if (dir.exists()) deleteDirectory(dir);

    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete(); // 删除空目录或文件
    }

    @Override
    public void renewDownloader(){
        // 用于刷新下载器的一些状态
        for (Downloader downloader : downloaders.values()){
            try {
                downloader.setReady(downloader.connect());
            }catch (Exception e){
                continue;
            }
        }
    }

    @Override
    public void checkDownloaderTask(){

        // 检查下载器内的任务状态
        for (Map.Entry<Integer,Downloader> entry: downloaders.entrySet()){
            Integer downloaderId = entry.getKey();
            Downloader downloader = entry.getValue();

            if (!downloader.isReady()){
                continue;
            }

            try {
                List<DownloaderTask> tasks = downloader.getAllTask();
                int taskCount = 0;
                for (DownloaderTask task : tasks){
                    // 任务异常状态时
                    if (task.getState().equals("error")){
                        String index = task.getTaskIndex();

                        // 更改任务列表为异常
                        DownloadTaskDTO taskDTO = downloadTaskService.queryByIndex(index);

                        if (taskDTO == null){
                            // 说明不是程序添加的，同样也删除
                            continue;
                        }

                        taskDTO.setState(DownloadState.DOWNLOADING.ordinal());
                        downloadTaskService.update(taskDTO);

                        // 在任务异常时删除下载器内的，由用户发起重试
                        downloader.deleteTask(taskDTO.getTaskIndex());
                    }else if (task.isCompleted()){
                        String index = task.getTaskIndex();
                        DownloadTaskDTO taskDTO = downloadTaskService.queryByIndex(index);

                        if (taskDTO == null){
                            // 说明不是程序添加的
                            continue;
                        }

                        String savePath = "temp/downloads/" + taskDTO.getTag();
                        File dir = new File(savePath);

                        // 只有未完成的任务需要处理
                        if (!taskDTO.getState().equals(DownloadState.FINISH.ordinal())){
                            File[] files = dir.listFiles();

                            if (files != null) {
                                for (File i: files)
                                    createFileInfo(i, taskDTO.getRoot(), taskDTO.getParentIndex(),
                                            taskDTO.getUserId(),downloader);

                            }

                            taskDTO.setState(DownloadState.FINISH.ordinal());
                            downloadTaskService.update(taskDTO);
                        }

                        downloader.deleteTask(taskDTO.getTaskIndex());

                        if (dir.exists())
                            deleteDirectory(dir);// 删除本地文件

                        // 如果是rss发起的任务则需设置rss任务完成
                        if (taskDTO.getOriginType().equals(DownloadOrigin.RSS.ordinal())){
                            rssService.setTaskFinish(taskDTO.getOriginId());
                        }

                    } else {
                        taskCount++;
                    }



                }
                // 如果存在空余位置
                if(taskCount < downloader.getAvailable()){
                    List<DownloadTaskDTO> taskDTOs =
                            downloadTaskService.fetchWait(downloader.getAvailable() - taskCount,downloaderId);

                    for (DownloadTaskDTO taskDTO : taskDTOs){
                        String taskIndex = downloader.addTask(taskDTO.getTag(), taskDTO.getUrl());
                        if (StringUtil.isBlank(taskIndex)){
                            // 无法获得索引说明异常
                            taskDTO.setTaskIndex(taskIndex);
                            taskDTO.setState(DownloadState.ERROR.ordinal());
                        }else{
                            taskDTO.setTaskIndex(taskIndex);
                            taskDTO.setState(DownloadState.DOWNLOADING.ordinal());
                        }
                        downloadTaskService.update(taskDTO);
                    }

                }

            }catch (Exception e){
                log.error(e.getMessage());
                continue;
            }
        }
    }

    private void createFileInfo(File file, String root, String parent, Long userId, Downloader downloader) {
        ListFiler listFiler = fileStorageService.getListFiler(root);
        if (file.isDirectory()){
            VFileDTO dir = listFiler.makeDirectory(parent, file.getName(), userId);

            File[] files = file.listFiles();
            for (File i: files)
                createFileInfo(i, root, parent, userId, downloader);
        }
        else if (file.isFile()){
            // 过滤一部分无用文件
            if (downloader.filter(file.getName())){
                String hash = MD5Util.getMd5(file);
                listFiler.upload(parent,file,file.getName(),file.length(),userId,hash);
            }
        }
    }

}
