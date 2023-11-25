package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.convert.DeviceConvert;
import com.hamsterhub.device.Storage;
import com.hamsterhub.device.ext.LocalDisk;
import com.hamsterhub.response.Response;
import com.hamsterhub.service.FileService;
import com.hamsterhub.service.StorageService;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.service.AccountService;
import com.hamsterhub.service.service.DeviceService;
import com.hamsterhub.util.SecurityUtil;
import com.hamsterhub.vo.DeviceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "存储设备 数据接口")
public class DeviceController {
    @Autowired
    private StorageService storageService;
    @Autowired
    private DeviceService deviceService;

    @ApiOperation("设备类型")
    @GetMapping(value = "/deviceType")
    public Response deviceType() {
        return Response.success().data(storageService.getTypes());
    }

    @ApiOperation("设备列表(token)")
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
        if (storageService.isTypeExist(deviceVO.getType()))
            throw new BusinessException(CommonErrorCode.E_300004);

        DeviceDTO deviceDTO = DeviceConvert.INSTANCE.vo2dto(deviceVO);
        Storage storage = storageService.getInstance(deviceDTO);
        // 连接测试
        if (!storage.verify())
            throw new BusinessException(CommonErrorCode.E_300006);

        DeviceDTO data = deviceService.create(deviceDTO);
        return Response.success().data(data);
    }

    @ApiOperation("修改设备(token)")
    @PostMapping(value = "/modifyDevice")
    @Token
    public Response modifyDevice(@RequestBody DeviceVO deviceVO) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 权限不足
        if (!accountDTO.isAdmin())
            throw new BusinessException(CommonErrorCode.E_NO_PERMISSION);
        // 设备不存在
        if (!deviceService.isExist(deviceVO.getId()))
            throw new BusinessException(CommonErrorCode.E_300001);
        // 不存在该设备类型编号
        if (storageService.isTypeExist(deviceVO.getType()))
            throw new BusinessException(CommonErrorCode.E_300004);

        DeviceDTO deviceDTO = DeviceConvert.INSTANCE.vo2dto(deviceVO);
        Storage storage = storageService.getInstance(deviceDTO);
        // 连接测试
        if (!storage.verify())
            throw new BusinessException(CommonErrorCode.E_300006);

        deviceService.update(deviceDTO);
        return Response.success();
    }

    @ApiOperation("删除设备(token)")
    @PostMapping(value = "/deleteDevice")
    @Token
    public Response deleteDevice(@RequestBody Long deviceId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 权限不足
        if (!accountDTO.isAdmin())
            throw new BusinessException(CommonErrorCode.E_NO_PERMISSION);
        // 设备不存在
        if (!deviceService.isExist(deviceId))
            throw new BusinessException(CommonErrorCode.E_300001);

        deviceService.delete(deviceId);
        return Response.success();
    }

}
