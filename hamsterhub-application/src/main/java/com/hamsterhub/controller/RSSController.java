package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.convert.RSSConvert;
import com.hamsterhub.convert.TaskConvert;
import com.hamsterhub.response.RSSListResponse;
import com.hamsterhub.response.RSSTaskResponse;
import com.hamsterhub.response.Response;
import com.hamsterhub.response.TaskResponse;
import com.hamsterhub.service.downloader.DownloadService;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.dto.DownloadTaskListDTO;
import com.hamsterhub.service.dto.RSSListDTO;
import com.hamsterhub.service.dto.RSSTaskDTO;
import com.hamsterhub.service.service.RSSService;
import com.hamsterhub.util.SecurityUtil;
import com.hamsterhub.vo.RSSListVO;
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
public class RSSController {

    @Autowired
    private RSSService rssService;


    @Operation(summary ="添加RSS列表")
    @PostMapping(value = "/rss/add")
    @Token
    public Response addRSS(@RequestParam("root") String root,
                            @RequestParam("parent") String parent,
                            @RequestParam("url") String url,
                            @RequestParam(value = "name", required = false) String name,
                            @RequestParam(value = "downloadId", required = false) Integer downloadId,
                           @RequestParam(value = "replaceHost", required = false) String replaceHost,
                           @RequestParam(value = "mirrorHost", required = false) String mirrorHost

    ) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        if (downloadId == null) {
            downloadId = 1;
        }

        if (StringUtil.isBlank(name)){
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            name = formatter.format(date) + " RSS";
        }

        RSSListDTO rssListDTO = RSSListDTO.createRSSListDTO(url, accountDTO.getId(), root,
                parent, name, replaceHost,mirrorHost);

        rssService.createRSSList(rssListDTO);
        return Response.success().msg("创建成功");
    }

    @Operation(summary ="RSS列表(token)")
    @GetMapping(value = "/rss/list")
    @Token
    public Response rssList() {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        List<RSSListDTO> list = rssService.queryRSSListByUser(accountDTO.getId());
        List<RSSListResponse> res  = RSSConvert.INSTANCE.dto2resBatch(list);
        return Response.success().data(res);
    }

    @Operation(summary ="删除RSS(token)")
    @PostMapping(value = "/rss/del")
    @Token
    public Response deleteRSS(@RequestParam("id") Long id) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        rssService.deleteRSSList(id,accountDTO.getId());
        return Response.success().msg("删除成功");
    }

    @Operation(summary ="启用和关闭RSS订阅(token)")
    @PostMapping(value = "/rss/enable")
    @Token
    public Response changeRSSEnable(@RequestParam("id") Long id,@RequestParam("enable") Integer enable) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        rssService.setEnable(id, enable != null && enable == 1,accountDTO.getId());
        return Response.success().msg("设置成功");
    }

    @Operation(summary ="编辑RSS列表")
    @PostMapping(value = "/rss/update")
    @Token
    public Response editRSS(@RequestBody RSSListVO rssList) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        RSSListDTO rssListDTO = RSSConvert.INSTANCE.vo2dto(rssList);
        // todo 基于用户组的权限控制
        rssListDTO.setUserId(accountDTO.getId());
        rssService.updateRSSListForUser(rssListDTO);
        return Response.success().msg("修改成功");
    }

    @Operation(summary ="RSS获取的记录列表(token)")
    @GetMapping(value = "/rss/task/list")
    @Token
    public Response rssTaskList(@RequestParam(value = "id",required = false) Long id) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        List<RSSTaskDTO> list = rssService.queryRSSTasks(id,accountDTO.getId());
        List<RSSTaskResponse> res  = RSSConvert.INSTANCE.dto2resBatchForTask(list);
        return Response.success().data(res);
    }

    @Operation(summary ="删除RSS获取的记录(token)")
    @PostMapping(value = "/rss/task/del")
    @Token
    public Response delRssTask(@RequestParam(value = "id") Long id) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        rssService.deleteRSSTaskForUser(id,accountDTO.getId());
        return Response.success().msg("删除成功");
    }

}
