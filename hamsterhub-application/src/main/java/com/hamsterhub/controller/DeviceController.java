package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.convert.DeviceConvert;
import com.hamsterhub.device.Storage;
import com.hamsterhub.response.DeviceResponse;
import com.hamsterhub.response.SizeResponse;
import com.hamsterhub.response.Response;
import com.hamsterhub.service.StorageService;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.service.DeviceService;
import com.hamsterhub.service.service.DeviceStrategyService;
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
    @Autowired
    private DeviceStrategyService deviceStrategyService;

    @ApiOperation("设备类型")
    @GetMapping(value = "/deviceType")
    public Response deviceType() {
        return Response.success().data(storageService.getTypes());
    }

    @ApiOperation("设备列表(admin)")
    @GetMapping(value = "/queryDevice")
    @Token("1")
    public Response queryDevice() {
        List<DeviceDTO> data = deviceService.queryBatch();
        List<DeviceResponse> res = DeviceConvert.INSTANCE.dto2resBatch(data);
        for (DeviceResponse i: res) {
            if (i.getConfigured())
                i.setStrategyId(deviceStrategyService.queryStrategyId(Long.parseLong(i.getId())).toString());
            Storage storage = storageService.getInstance(DeviceConvert.INSTANCE.res2dto(i));
            i.setSize(new SizeResponse(storage.getTotalSize(), storage.getUsableSize()));
        }

        return Response.success().data(res);
    }

    @ApiOperation("创建设备(admin)")
    @PostMapping(value = "/createDevice")
    @Token("1")
    public Response createDevice(@RequestBody DeviceVO deviceVO) {
        // 不存在该设备类型编号
        if (storageService.isTypeExist(deviceVO.getType()))
            throw new BusinessException(CommonErrorCode.E_300004);

        DeviceDTO deviceDTO = DeviceConvert.INSTANCE.vo2dto(deviceVO);
        // 连接测试
        if (!storageService.verify(deviceDTO))
            throw new BusinessException(CommonErrorCode.E_300006);

        DeviceDTO data = deviceService.create(deviceDTO);
        return Response.success().data(DeviceConvert.INSTANCE.dto2res(data));
    }

    @ApiOperation("修改设备(admin)")
    @PostMapping(value = "/modifyDevice")
    @Token("1")
    public Response modifyDevice(@RequestBody DeviceVO deviceVO) {
        // 设备不存在
        if (!deviceService.isExist(deviceVO.getId()))
            throw new BusinessException(CommonErrorCode.E_300001);
        // 不存在该设备类型编号
        if (storageService.isTypeExist(deviceVO.getType()))
            throw new BusinessException(CommonErrorCode.E_300004);

        DeviceDTO deviceDTO = DeviceConvert.INSTANCE.vo2dto(deviceVO);
        // 连接测试
        if (!storageService.verify(deviceDTO))
            throw new BusinessException(CommonErrorCode.E_300006);

        deviceService.update(deviceDTO);
        return Response.success().msg("设备修改成功");
    }

    @ApiOperation("删除设备(admin)")
    @PostMapping(value = "/deleteDevice")
    @Token("1")
    public Response deleteDevice(@RequestParam("deviceId") Long deviceId) {
        // 设备不存在
        if (!deviceService.isExist(deviceId))
            throw new BusinessException(CommonErrorCode.E_300001);

        deviceService.delete(deviceId);
        return Response.success().msg("设备删除成功");
    }

}
