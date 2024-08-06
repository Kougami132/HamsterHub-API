package com.hamsterhub.service.downloader.impl;


import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.MD5Util;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.service.device.ListFiler;
import com.hamsterhub.service.downloader.DownloadOrigin;
import com.hamsterhub.service.downloader.DownloadService;
import com.hamsterhub.service.downloader.DownloadState;
import com.hamsterhub.service.downloader.DownloadType;
import com.hamsterhub.service.downloader.ext.Downloader;
import com.hamsterhub.service.downloader.ext.DownloaderTask;
import com.hamsterhub.service.downloader.ext.impl.QBittorrentDownloader;
import com.hamsterhub.service.dto.*;
import com.hamsterhub.service.service.DownloadTaskListService;
import com.hamsterhub.service.service.FileStorageService;
import com.hamsterhub.service.service.RSSService;
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
    private DownloadTaskListService downloadTaskListService;

    @Autowired
    private RSSService rssService;

    private final Map<Integer, Downloader> downloaders = new HashMap<>();

    @Override
    @PostConstruct
    public void loadData(){
        downloaders.clear();

        Downloader downloader = new QBittorrentDownloader(qBitAddress, qBitUsername, qBitPassword);

        if (downloader.connect()){
            downloader.setReady(true);
            downloaders.put(1, downloader);
        }

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
    public String addDownloadTaskForUser(AccountDTO user, Integer downloaderId, String root,
                                         String parent, String url, String name){
        // 获取下载器
        Downloader downloader = getDownloader(downloaderId);

        // 检查父目录
        VFileDTO file = fileStorageService.getFile(root, parent, user);
        CommonErrorCode.checkAndThrow(file == null, CommonErrorCode.E_600001);
        CommonErrorCode.checkAndThrow(!file.isDir(), CommonErrorCode.E_600013);

        String tag = StringUtil.generateRandomString(16);

        DownloadTaskListDTO taskDTO = DownloadTaskListDTO.createTask(root,parent, DownloadOrigin.USER.ordinal(),
                user.getId(), DownloadType.URL.ordinal(),url ,downloaderId,user.getId(), tag, name);

        downloadTaskListService.create(taskDTO);
        return tag;
    }

//    public String addDownloadTaskForRSS(DownloadTaskListDTO taskDTO){
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
//        DownloadTaskListDTO taskDTO = DownloadTaskListDTO.createTask(root,parent, DownloadOrigin.USER.ordinal(),
//                user.getId(), DownloadType.URL.ordinal(),url ,downloaderId,user.getId(), tag);
//
//        downloadTaskListService.create(taskDTO);
//        return tag;
//    }

    @Override
    public List<DownloadTaskListDTO> getList(AccountDTO user){
        List<DownloadTaskListDTO> downloadTaskListDTOS = downloadTaskListService.fetchByUser(user.getId());

        for (DownloadTaskListDTO taskDTO : downloadTaskListDTOS){
            if (taskDTO.getState().equals(0)){
                // 等于0就不用获取信息了
                break;
            }

            if (taskDTO.getState().equals(DownloadState.DOWNLOADING.ordinal())){
                DownloaderTask downloaderTask = null;
                try {
                    // 将有进度的部分存入
                    Downloader downloader = getDownloader(taskDTO.getDownloader());
                    downloaderTask = downloader.getTask(taskDTO.getTag());
                } catch (Exception e) {
                    continue;
                }

                if (downloaderTask != null){
                    taskDTO.setCompleted(downloaderTask.getCompleted());
                    taskDTO.setTotal(downloaderTask.getTotal_size());
                }
            }
        }

        return downloadTaskListDTOS;
    }

    @Override
    public void deleteDownloadTask(Integer downloaderId, String tag, AccountDTO user){
        Downloader downloader = getDownloader(downloaderId);
        DownloadTaskListDTO TaskDTO = downloadTaskListService.query(user.getId(), tag);

        // 防止越权
        CommonErrorCode.checkAndThrow(TaskDTO == null, CommonErrorCode.E_100007);

        downloader.deleteTask(tag);
        downloadTaskListService.delete(tag);
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
        for (Downloader downloader : downloaders.values()){
            if (!downloader.isReady()){
                continue;
            }

            try {
                List<DownloaderTask> tasks = downloader.getAllTask();
                int taskCount = 0;
                for (DownloaderTask task : tasks){
                    // 任务异常状态时
                    if (task.getState().equals("error")){
                        String tags = task.getTags();
                        // 更改任务列表为异常
                        DownloadTaskListDTO taskDTO = downloadTaskListService.query(tags);
                        taskDTO.setState(DownloadState.DOWNLOADING.ordinal());
                        downloadTaskListService.update(taskDTO);

                        // 在任务异常时删除下载器内的，由用户发起重试
                        downloader.deleteTask(tags);
                    }else if (task.isCompleted()){
                        String tags = task.getTags();
                        DownloadTaskListDTO taskDTO = downloadTaskListService.query(tags);

                        if (taskDTO == null){
                            // 说明不是程序添加的
                            continue;
                        }

                        String savePath = "temp/downloads/" + taskDTO.getTag();
                        File dir = new File(savePath);

                        File[] files = dir.listFiles();
                        for (File i: files)
                            createFileInfo(i, taskDTO.getRoot(), taskDTO.getParentIndex(), taskDTO.getUserId());

                        taskDTO.setState(DownloadState.FINISH.ordinal());
                        downloadTaskListService.update(taskDTO);
                        downloader.deleteTask(tags);
                        dir.delete();// 删除临时目录

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
                    List<DownloadTaskListDTO> taskDTOs =
                            downloadTaskListService.fetchWait(downloader.getAvailable() - taskCount);

                    for (DownloadTaskListDTO taskDTO : taskDTOs){
                        downloader.addTask(taskDTO.getTag(),taskDTO.getUrl());
                        taskDTO.setState(DownloadState.DOWNLOADING.ordinal());
                        downloadTaskListService.update(taskDTO);
                    }

                }

            }catch (Exception e){
                log.error(e.getMessage());
                continue;
            }
        }
    }

//    private void createFileInfo(File file, Long strategyId, Long parentId, Long accountId) {
//        if (file.isDirectory()) {
//            VFileDTO dir = VFileDTO.newDir(file.getName(), strategyId, parentId, accountId);
//            dir = vFileService.createDir(dir);
//
//            File[] files = file.listFiles();
//            for (File i: files)
//                createFileInfo(i, strategyId, Long.parseLong(dir.getId()), accountId);
//        }
//        else if (file.isFile()) {
//            StrategyDTO strategyDTO = strategyService.query(strategyId);
//            String hash = MD5Util.getMd5(file);
//            RFileDTO rFileDTO;
//            if (rFileService.isExist(hash, strategyId))
//                rFileDTO = rFileService.query(hash, strategyId);
//            else
//                rFileDTO = fileService.upload(file, strategyDTO);
//            VFileDTO f = VFileDTO.newFile(file.getName(), strategyId, parentId, rFileDTO, accountId);
//            vFileService.create(f);
//        }
//    }

    private void createFileInfo(File file, String root, String parent, Long userId) {
        ListFiler listFiler = fileStorageService.getListFiler(root);
        if (file.isDirectory()){
            VFileDTO dir = listFiler.makeDirectory(parent, file.getName(), userId);

            File[] files = file.listFiles();
            for (File i: files)
                createFileInfo(i, root, parent, userId);
        }
        else if (file.isFile()){
            String hash = MD5Util.getMd5(file);
            listFiler.upload(parent,file,file.getName(),file.length(),userId,hash);
        }
    }

}
