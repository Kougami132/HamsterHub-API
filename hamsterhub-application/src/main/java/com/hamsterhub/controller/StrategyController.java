package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import com.hamsterhub.convert.StrategyConvert;
import com.hamsterhub.device.Storage;
import com.hamsterhub.response.Response;
import com.hamsterhub.response.SizeResponse;
import com.hamsterhub.response.StrategyResponse;
import com.hamsterhub.service.StorageService;
import com.hamsterhub.service.dto.AccountDTO;
import com.hamsterhub.service.dto.DeviceDTO;
import com.hamsterhub.service.dto.DeviceStrategyDTO;
import com.hamsterhub.service.dto.StrategyDTO;
import com.hamsterhub.service.service.DeviceService;
import com.hamsterhub.service.service.DeviceStrategyService;
import com.hamsterhub.service.service.StrategyService;
import com.hamsterhub.util.SecurityUtil;
import com.hamsterhub.vo.StrategyVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@RestController
@Api(tags = "存储策略 数据接口")
public class StrategyController {

    private List<String> TYPE = Stream.of("聚合", "备份").collect(toList()),
                         MODE = Stream.of("优先存储较大剩余容量设备", "优先存储较小剩余容量设备").collect(toList());

    @Autowired
    private DeviceService deviceService;
    @Autowired
    private StrategyService strategyService;
    @Autowired
    private DeviceStrategyService deviceStrategyService;
    @Autowired
    private StorageService storageService;

    @ApiOperation("策略类型")
    @GetMapping(value = "/strategyType")
    public Response strategyType() {
        return Response.success().data(TYPE);
    }

    @ApiOperation("策略模式")
    @GetMapping(value = "/strategyMode")
    public Response strategyMode() {
        return Response.success().data(MODE);
    }

    @ApiOperation("策略列表(token)")
    @GetMapping(value = "/queryStrategy")
    @Token
    public Response queryStrategy() {
//        AccountDTO accountDTO = SecurityUtil.getAccount();
//        // 权限不足
//        if (!accountDTO.isAdmin())
//            throw new BusinessException(CommonErrorCode.E_NO_PERMISSION);

        List<StrategyResponse> data = StrategyConvert.INSTANCE.dto2resBatch(strategyService.queryBatch());
        for (StrategyResponse i: data)
            i.setDeviceIds(deviceStrategyService.queryDeviceIds(Long.parseLong(i.getId())));
        return Response.success().data(data);
    }

    @ApiOperation("创建策略(admin)")
    @PostMapping(value = "/createStrategy")
    @Token
    public Response createStrategy(@RequestBody StrategyVO strategyVO) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 权限不足
        if (!accountDTO.isAdmin())
            throw new BusinessException(CommonErrorCode.E_NO_PERMISSION);
        // 不存在该策略类型
        if (strategyVO.getType() < 0 || strategyVO.getType() >= TYPE.size())
            throw new BusinessException(CommonErrorCode.E_400004);
        // 不存在该策略模式
        if (strategyVO.getMode() < 0 || strategyVO.getMode() >= MODE.size())
            throw new BusinessException(CommonErrorCode.E_400005);
        // 设备不存在
        for (Long i: strategyVO.getDeviceIds())
            if (!deviceService.isExist(i))
                throw new BusinessException(CommonErrorCode.E_300001);
        // 设备已绑定其他策略
        for (Long i: strategyVO.getDeviceIds())
            if (deviceStrategyService.isDeviceExist(i))
                throw new BusinessException(CommonErrorCode.E_300003);

        StrategyDTO data = strategyService.create(StrategyConvert.INSTANCE.vo2dto(strategyVO));
        for (Long i: strategyVO.getDeviceIds())
            deviceStrategyService.create(new DeviceStrategyDTO(i, data.getId()));
        return Response.success().data(StrategyConvert.INSTANCE.dto2res(data));
    }

    @ApiOperation("修改策略(admin)")
    @PostMapping(value = "/modifyStrategy")
    @Token
    public Response modifyStrategy(@RequestBody StrategyVO strategyVO) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 权限不足
        if (!accountDTO.isAdmin())
            throw new BusinessException(CommonErrorCode.E_NO_PERMISSION);
        // 策略不存在
        if (!strategyService.isExist(strategyVO.getId()))
            throw new BusinessException(CommonErrorCode.E_400001);
        // 不存在该策略类型
        if (strategyVO.getType() < 0 || strategyVO.getType() >= TYPE.size())
            throw new BusinessException(CommonErrorCode.E_400004);
        // 不存在该策略模式
        if (strategyVO.getMode() < 0 || strategyVO.getMode() >= MODE.size())
            throw new BusinessException(CommonErrorCode.E_400005);
        // 设备不存在
        for (Long i: strategyVO.getDeviceIds())
            if (!deviceService.isExist(i))
                throw new BusinessException(CommonErrorCode.E_300001);
        // 设备已绑定其他策略
        for (Long i: strategyVO.getDeviceIds()) {
            Long strategyId = deviceStrategyService.queryStrategyId(i);
            if (strategyId != strategyVO.getId())
                throw new BusinessException(CommonErrorCode.E_300003);
        }

        strategyService.update(StrategyConvert.INSTANCE.vo2dto(strategyVO));
        // 获取策略已绑定的设备，多的新增，少的删除
        List<Long> deviceIds = deviceStrategyService.queryDeviceIds(strategyVO.getId());
        for (Long i: strategyVO.getDeviceIds())
            if (!deviceIds.contains(i))
                deviceStrategyService.create(new DeviceStrategyDTO(strategyVO.getId(), i));
        for (Long i: deviceIds)
            if (!strategyVO.getDeviceIds().contains(i))
                deviceStrategyService.deleteByDeviceId(i);
        return Response.success();
    }

    @ApiOperation("删除策略(admin)")
    @PostMapping(value = "/deleteStrategy")
    @Token
    public Response deleteStrategy(@RequestParam("strategyId") Long strategyId) {
        AccountDTO accountDTO = SecurityUtil.getAccount();
        // 权限不足
        if (!accountDTO.isAdmin())
            throw new BusinessException(CommonErrorCode.E_NO_PERMISSION);
        // 策略不存在
        if (!strategyService.isExist(strategyId))
            throw new BusinessException(CommonErrorCode.E_400001);

        strategyService.delete(strategyId);
        deviceStrategyService.deleteByStrategyId(strategyId);
        return Response.success();
    }

    @ApiOperation("获取策略存储空间(token)")
    @GetMapping(value = "/queryStrategySize")
    @Token
    public Response queryStrategySize(@RequestParam("root") String root) {
        StrategyDTO strategyDTO = strategyService.query(root);
        List<Long> deviceIds = deviceStrategyService.queryDeviceIds(strategyDTO.getId());
        SizeResponse data = new SizeResponse();
        for (Long i: deviceIds) {
            DeviceDTO deviceDTO = deviceService.query(i);
            Storage storage = storageService.getInstance(deviceDTO);
            data.addTotal(storage.getTotalSize());
            data.addUsable(storage.getUsableSize());
        }
        return Response.success().data(data);
    }

}
