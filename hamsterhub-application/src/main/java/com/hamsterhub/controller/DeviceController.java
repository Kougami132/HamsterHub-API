package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.convert.DeviceConvert;
import com.hamsterhub.response.Response;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.service.AccountService;
import com.hamsterhub.service.service.DeviceService;
import com.hamsterhub.util.SecurityUtil;
import com.hamsterhub.vo.DeviceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toList;

@RestController
@Api(tags = "存储设备 数据接口")
public class DeviceController {

    private List<String> TYPE = Stream.of("本地", "阿里云").collect(toList());

    @Resource
    private DeviceService deviceService;
    @Resource
    private AccountService accountService;

    @ApiOperation("设备类型种类列表")
    @GetMapping(value = "/deviceType")
    public Response createDevice() {
        return Response.success().data(TYPE);
    }

    @ApiOperation("设备类型列表(token)")
    @GetMapping(value = "/queryDevice")
    @Token
    public Response queryDevice() {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 权限不足
        if (!accountDTO.isAdmin())
            throw new BusinessException(CommonErrorCode.E_NO_PERMISSION);

        List<DeviceDTO> data = deviceService.queryBatch();
        return Response.success().data(data);
    }

    @ApiOperation("创建设备(token)")
    @PostMapping(value = "/createDevice")
    @Token
    public Response createDevice(@RequestBody DeviceVO deviceVO) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 权限不足
        if (!accountDTO.isAdmin())
            throw new BusinessException(CommonErrorCode.E_NO_PERMISSION);
        // 不存在该设备类型编号
        if (deviceVO.getType() < 0 || deviceVO.getType() >= TYPE.size())
            throw new BusinessException(CommonErrorCode.E_300004);

        DeviceDTO data = deviceService.create(DeviceConvert.INSTANCE.vo2dto(deviceVO));
        return Response.success().data(data);
    }



}
