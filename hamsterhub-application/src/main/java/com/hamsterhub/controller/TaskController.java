package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.convert.DownloadTaskConvert;
import com.hamsterhub.response.DownloaderOptionResponse;
import com.hamsterhub.response.Response;
import com.hamsterhub.response.TaskResponse;
import com.hamsterhub.service.downloader.DownloadService;
import com.hamsterhub.database.dto.AccountDTO;
import com.hamsterhub.database.dto.DownloadTaskDTO;
import com.hamsterhub.service.entity.DownloaderOption;
import com.hamsterhub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


@RestController
@Tag(name = "任务管理 数据接口")
@RequestMapping("api")
public class TaskController {

    @Autowired
    private DownloadService downloadService;

    @Operation(summary ="离线下载(token)")
    @PostMapping(value = "/downloadTask")
    @Token
    public Response downloadOffline(@RequestParam("root") String root,
                                    @RequestParam("parentId") String parent,
                                    @RequestParam("url") String url,
                                    @RequestParam(value = "name", required = false) String name,
                                    @RequestParam(value = "downloadId", required = false) Integer downloadId

    ) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        if (downloadId == null) {
            downloadId = 1;
        }

        if (StringUtil.isBlank(name)){
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 为空则使用当前日期
            name = formatter.format(date) + " task";
        }

        String tag = downloadService.addDownloadTaskForUser(accountDTO, downloadId,root,parent,url,name);

        return Response.success().msg("下载请求已加入队列").data(tag);
    }

    @Operation(summary ="离线下载任务列表(token)")
    @GetMapping(value = "/taskList")
    @Token
    public Response taskList() {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        List<DownloadTaskDTO> list = downloadService.getList(accountDTO);
        List<TaskResponse> res  = DownloadTaskConvert.INSTANCE.dto2resBatch(list);
        return Response.success().data(res);
    }

    @Operation(summary ="删除任务(token)")
    @PostMapping(value = "/deleteTask")
    @Token
    public Response deleteTask(@RequestParam("tag") String tag,
                               @RequestParam(value = "downloadId", required = false) Integer downloadId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();

        if (downloadId == null) {
            downloadId = 1;
        }

        downloadService.deleteDownloadTask(downloadId,tag,accountDTO);
        return Response.success().msg("删除成功");
    }

    @Operation(summary ="获取下载器")
    @GetMapping(value = "/downloaderList")
    @Token
    public Response getDownloaderList() {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        List<DownloaderOption> list = downloadService.getDownloaderOption(accountDTO);
        List<DownloaderOptionResponse> res  = DownloadTaskConvert.INSTANCE.dto2resBatchForOption(list);
        return Response.success().data(res);
    }
}
