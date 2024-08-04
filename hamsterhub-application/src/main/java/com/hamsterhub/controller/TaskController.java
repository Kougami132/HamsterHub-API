package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.convert.TaskConvert;
import com.hamsterhub.response.Response;
import com.hamsterhub.response.TaskResponse;
import com.hamsterhub.service.BitTorrentService;
//import com.hamsterhub.service.DownloadService;
import com.hamsterhub.common.service.RedisService;
import com.hamsterhub.service.convert.DownloadTaskListConvert;
import com.hamsterhub.service.downloader.DownloadService;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.dto.DownloadTaskListDTO;
import com.hamsterhub.service.entity.Torrent;
import com.hamsterhub.service.service.StrategyService;
import com.hamsterhub.service.service.VFileService;
import com.hamsterhub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

@RestController
@Tag(name = "任务管理 数据接口")
public class TaskController {

    @Autowired
    private RedisService redisService;
    @Autowired
    private BitTorrentService bitTorrentService;
    @Autowired
    private VFileService vFileService;
    @Autowired
    private StrategyService strategyService;
    @Autowired
    private DownloadService downloadService;

    @Operation(summary ="离线下载(token)")
    @PostMapping(value = "/downloadTask")
    @Token
    public Response downloadOffline(@RequestParam("root") String root,
                                    @RequestParam("parentId") String parent,
                                    @RequestParam("url") String url,
                                    @RequestParam(value = "downloadId", required = false) Integer downloadId

    ) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
//        if (!bitTorrentService.connect())
//            throw new BusinessException(CommonErrorCode.E_700003);
//
//        // 保存路径不是目录
//        if (parentId != 0 && !vFileService.query(parentId).isDir())
//            throw new BusinessException(CommonErrorCode.E_600013);
//
//        String tag = StringUtil.generateRandomString(16);
//        redisService.addTask(accountDTO.getId(), tag);
//        try {
//            downloadService.sendDownloadMsg(tag, url, strategyService.query(root).getId(), parentId, accountDTO.getId());
//        } catch (RejectedExecutionException e) {
//            throw new BusinessException(CommonErrorCode.E_100008);
//        }
        if (downloadId == null) {
            downloadId = 1;
        }

        String tag = downloadService.addDownloadTaskForUser(accountDTO, downloadId,root,parent,url);


        return Response.success().msg("下载请求已加入队列").data(tag);
    }

    @Operation(summary ="离线下载任务列表(token)")
    @GetMapping(value = "/taskList")
    @Token
    public Response taskList() {
        AccountDTO accountDTO = SecurityUtil.getAccount();
//        Map<String, String> tasks = redisService.getTasks(accountDTO.getId());
//        if (!bitTorrentService.connect())
//            throw new BusinessException(CommonErrorCode.E_700003);
//        for (String i: tasks.keySet()) {
//            TaskResponse task = new TaskResponse();
//            task.setTag(i);
//            task.setState(tasks.get(i));
//            Torrent torrent = bitTorrentService.getTorrent(i);
//            if (torrent != null) {
//                if (!task.getState().equals("done")) task.setState(torrent.getState());
//                task.setName(torrent.getName());
//                task.setCompleted(torrent.getCompleted());
//                task.setTotal(torrent.getTotal_size());
//            }
//            res.add(task);
//        }

        List<DownloadTaskListDTO> list = downloadService.getList(accountDTO);
        List<TaskResponse> res  = TaskConvert.INSTANCE.dto2resBatch(list);
        return Response.success().data(res);
    }

    @Operation(summary ="删除任务(token)")
    @PostMapping(value = "/deleteTask")
    @Token
    public Response deleteTask(@RequestParam("tag") String tag,
                               @RequestParam(value = "downloadId", required = false) Integer downloadId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
//        Map<String, String> tasks;
//        if (accountDTO.isAdmin()) tasks = redisService.getTasks();
//        else tasks = redisService.getTasks(accountDTO.getId());
//        if (!tasks.containsKey(tag))
//            throw new BusinessException(CommonErrorCode.E_100007);
//        redisService.removeTask(tag);
//        bitTorrentService.deleteTorrent(tag);

        if (downloadId == null) {
            downloadId = 1;
        }

        downloadService.deleteDownloadTask(downloadId,tag,accountDTO);
        return Response.success().msg("删除成功");
    }
}
