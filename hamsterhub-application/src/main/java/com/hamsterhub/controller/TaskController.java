package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.response.Response;
import com.hamsterhub.response.TaskResponse;
import com.hamsterhub.service.BitTorrentService;
import com.hamsterhub.service.ProducerService;
import com.hamsterhub.service.RedisService;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.entity.Torrent;
import com.hamsterhub.service.service.StrategyService;
import com.hamsterhub.service.service.VFileService;
import com.hamsterhub.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@Api(tags = "任务管理 数据接口")
public class TaskController {

    @Autowired
    private RedisService redisService;
    @Autowired
    private BitTorrentService bitTorrentService;
    @Autowired
    private ProducerService producerService;
    @Autowired
    private VFileService vFileService;
    @Autowired
    private StrategyService strategyService;

    @ApiOperation("离线下载(token)")
    @PostMapping(value = "/downloadTask")
    @Token
    public Response downloadOffline(@RequestParam("root") String root,
                                    @RequestParam("parentId") Long parentId,
                                    @RequestParam("url") String url) {
        if (!bitTorrentService.connect())
            throw new BusinessException(CommonErrorCode.E_700003);
        AccountDTO accountDTO = SecurityUtil.getAccount();

        // 保存路径不是目录
        if (parentId != 0 && !vFileService.query(parentId).isDir())
            throw new BusinessException(CommonErrorCode.E_600013);

        String tag = producerService.sendDownloadMsg(url, strategyService.query(root).getId(), parentId, accountDTO.getId());
        redisService.addTask(accountDTO.getId(), tag);

        return Response.success().msg("下载请求已加入队列");
    }

    @ApiOperation("离线下载任务列表(token)")
    @GetMapping(value = "/taskList")
    @Token
    public Response taskList() {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        Map<String, String> tasks = redisService.getTasks(accountDTO.getId());
        List<TaskResponse> res = new ArrayList<>();
        if (!bitTorrentService.connect())
            throw new BusinessException(CommonErrorCode.E_700003);
        for (String i: tasks.keySet()) {
            TaskResponse task = new TaskResponse();
            task.setTag(i);
            task.setState(tasks.get(i));
            Torrent torrent = bitTorrentService.getTorrent(i);
            if (torrent != null) {
                task.setState(torrent.getState());
                task.setCompleted(torrent.getCompleted());
                task.setTotal(torrent.getTotal_size());
            }
            res.add(task);
        }
        return Response.success().data(res);
    }

    @ApiOperation("离线下载任务列表(token)")
    @PostMapping(value = "/deleteTask")
    @Token
    public Response deleteTask(@RequestParam("tag") String tag) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        Map<String, String> tasks;
        if (accountDTO.isAdmin()) tasks = redisService.getTasks();
        else tasks = redisService.getTasks(accountDTO.getId());
        if (!tasks.containsKey(tag))
            throw new BusinessException(CommonErrorCode.E_100007);
        redisService.removeTask(tag);

        return Response.success().msg("删除成功");
    }
}
