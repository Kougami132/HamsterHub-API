package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.common.util.StringUtil;
import com.hamsterhub.convert.RSSConvert;
import com.hamsterhub.response.RSSListResponse;
import com.hamsterhub.response.RSSTaskResponse;
import com.hamsterhub.response.Response;
import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.database.dto.RSSListDTO;
import com.hamsterhub.database.dto.RSSTaskDTO;
import com.hamsterhub.database.service.RSSService;
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
    public Response addRSS(@RequestBody RSSListVO rssList) {
        UserDTO userDTO = SecurityUtil.getUser();
        CommonErrorCode.checkAndThrow(rssList.getDownloader() == null,CommonErrorCode.E_100010);
        CommonErrorCode.checkAndThrow( StringUtil.isBlank(rssList.getUrl()),CommonErrorCode.E_100010);
        CommonErrorCode.checkAndThrow( StringUtil.isBlank(rssList.getRoot()),CommonErrorCode.E_100010);
        CommonErrorCode.checkAndThrow( StringUtil.isBlank(rssList.getParentIndex()),CommonErrorCode.E_100010);

        if (StringUtil.isBlank(rssList.getName())){
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            rssList.setName(formatter.format(date) + " RSS");
        }

        RSSListDTO rssListDTO = RSSConvert.INSTANCE.vo2dto(rssList);

        rssListDTO.setUserId(userDTO.getId());
        rssListDTO.setState(1);
        rssService.createRSSList(rssListDTO);
        return Response.success().msg("创建成功");
    }

    @Operation(summary ="RSS列表(token)")
    @GetMapping(value = "/rss/list")
    @Token
    public Response rssList() {
        UserDTO userDTO = SecurityUtil.getUser();
        List<RSSListDTO> list = rssService.queryRSSListByUser(userDTO.getId());
        List<RSSListResponse> res  = RSSConvert.INSTANCE.dto2resBatch(list);
        return Response.success().data(res);
    }

    @Operation(summary ="删除RSS(token)")
    @PostMapping(value = "/rss/del")
    @Token
    public Response deleteRSS(@RequestParam("id") Long id) {
        UserDTO userDTO = SecurityUtil.getUser();
        rssService.deleteRSSList(id,userDTO.getId());
        return Response.success().msg("删除成功");
    }

    @Operation(summary ="启用和关闭RSS订阅(token)")
    @PostMapping(value = "/rss/enable")
    @Token
    public Response changeRSSEnable(@RequestParam("id") Long id,@RequestParam("enable") Integer enable) {
        UserDTO userDTO = SecurityUtil.getUser();
        rssService.setEnable(id, enable != null && enable == 1,userDTO.getId());
        return Response.success().msg("设置成功");
    }

    @Operation(summary ="编辑RSS列表")
    @PostMapping(value = "/rss/update")
    @Token
    public Response editRSS(@RequestBody RSSListVO rssList) {
        UserDTO userDTO = SecurityUtil.getUser();
        RSSListDTO rssListDTO = RSSConvert.INSTANCE.vo2dto(rssList);
        // todo 基于用户组的权限控制
        rssListDTO.setUserId(userDTO.getId());
        rssService.updateRSSListForUser(rssListDTO);
        return Response.success().msg("修改成功");
    }

    @Operation(summary ="RSS获取的记录列表(token)")
    @GetMapping(value = "/rss/task/list")
    @Token
    public Response rssTaskList(@RequestParam(value = "id",required = false) Long id) {
        UserDTO userDTO = SecurityUtil.getUser();
        List<RSSTaskDTO> list = rssService.queryRSSTasks(id,userDTO.getId());
        List<RSSTaskResponse> res  = RSSConvert.INSTANCE.dto2resBatchForTask(list);
        return Response.success().data(res);
    }

    @Operation(summary ="删除RSS获取的记录(token)")
    @PostMapping(value = "/rss/task/del")
    @Token
    public Response delRssTask(@RequestParam(value = "id") Long id) {
        UserDTO userDTO = SecurityUtil.getUser();
        rssService.deleteRSSTaskForUser(id,userDTO.getId());
        return Response.success().msg("删除成功");
    }

}
