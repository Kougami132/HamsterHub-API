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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        AccountDTO accountDTO = SecurityUtil.getAccount();

        List<StrategyResponse> data = new ArrayList<>();
        List<StrategyDTO> strategyDTOs = strategyService.queryBatch();
        for (StrategyDTO i: strategyDTOs) {
            StrategyResponse strategyResponse = StrategyConvert.INSTANCE.dto2res(i);
            strategyResponse.setPermissions(separatePermission(i.getPermission()));
            if (accountDTO.isAdmin() || strategyResponse.getPermissions().contains(accountDTO.getType())) {
                strategyResponse.setDeviceIds(deviceStrategyService.queryDeviceIds(i.getId()).stream()
                        .map(Objects::toString)
                        .collect(toList()));

                data.add(strategyResponse);
            }
        }

        return Response.success().data(data);
    }

    @ApiOperation("创建策略(admin)")
    @PostMapping(value = "/createStrategy")
    @Token("0")
    public Response createStrategy(@RequestBody StrategyVO strategyVO) {
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

        StrategyDTO strategyDTO = StrategyConvert.INSTANCE.vo2dto(strategyVO);
        strategyDTO.setPermission(mergePermission(strategyVO.getPermissions()));
        StrategyDTO data = strategyService.create(strategyDTO);
        for (Long i: strategyVO.getDeviceIds())
            deviceStrategyService.create(new DeviceStrategyDTO(i, data.getId()));
        StrategyResponse res = StrategyConvert.INSTANCE.dto2res(data);
        res.setDeviceIds(strategyVO.getDeviceIds().stream()
                                                  .map(Objects::toString)
                                                  .collect(toList()));
        res.setPermissions(strategyVO.getPermissions());

        return Response.success().data(res);
    }

    @ApiOperation("修改策略(admin)")
    @PostMapping(value = "/modifyStrategy")
    @Token("0")
    public Response modifyStrategy(@RequestBody StrategyVO strategyVO) {
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

        StrategyDTO strategyDTO = StrategyConvert.INSTANCE.vo2dto(strategyVO);
        strategyDTO.setPermission(mergePermission(strategyVO.getPermissions()));
        strategyService.update(strategyDTO);
        // 获取策略已绑定的设备，多的新增，少的删除
        List<Long> deviceIds = deviceStrategyService.queryDeviceIds(strategyVO.getId());
        for (Long i: strategyVO.getDeviceIds())
            if (!deviceIds.contains(i)) {
                // 设备已绑定其他策略
                if (deviceStrategyService.isDeviceExist(i))
                    throw new BusinessException(CommonErrorCode.E_300003);

                deviceStrategyService.create(new DeviceStrategyDTO(strategyVO.getId(), i));
            }

        for (Long i: deviceIds)
            if (!strategyVO.getDeviceIds().contains(i))
                deviceStrategyService.deleteByDeviceId(i);
        return Response.success().msg("策略修改成功");
    }

    @ApiOperation("删除策略(admin)")
    @PostMapping(value = "/deleteStrategy")
    @Token("0")
    public Response deleteStrategy(@RequestParam("strategyId") Long strategyId) {
        // 策略不存在
        if (!strategyService.isExist(strategyId))
            throw new BusinessException(CommonErrorCode.E_400001);

        strategyService.delete(strategyId);
        deviceStrategyService.deleteByStrategyId(strategyId);
        return Response.success().msg("策略删除成功");
    }

    private Integer mergePermission(List<Integer> permission) {
        // 清除重复项
        permission = permission.stream()
                .distinct()
                .collect(toList());

        int res = 0;
        for (int i: permission)
            res += 1 << i;
        return res;
    }
    private List<Integer> separatePermission(Integer permission) {
        List<Integer> res = new ArrayList<>();
        Integer count = 0;
        while (permission > 0) {
            if (permission % 2 == 1)
                res.add(count);
            count++;
            permission >>= 1;
        }
        return res;
    }

    private Boolean hasPermission(List<Integer> permission, Integer type) {
        return permission.contains(type);
    }

    @ApiOperation("策略容量(admin)")
    @GetMapping(value = "/queryStrategySize")
    public Response queryStrategySize(@RequestParam("strategyId") Long strategyId) {
        // 策略不存在
        if (!strategyService.isExist(strategyId))
            throw new BusinessException(CommonErrorCode.E_400001);

        StrategyDTO strategyDTO = strategyService.query(strategyId);
        List<Long> deviceIds = deviceStrategyService.queryDeviceIds(strategyDTO.getId());

        Long total = 0L, usable = 0L;
        for (Long deviceId: deviceIds) {
            DeviceDTO deviceDTO = deviceService.query(deviceId);
            Storage storage = storageService.getInstance(deviceDTO);
            // 设备连接失败
            if (!deviceDTO.isConnected())
                throw new BusinessException(CommonErrorCode.E_300006);
            total += storage.getTotalSize();
            usable += storage.getUsableSize();
        }
        SizeResponse size = new SizeResponse(total, usable);
        return Response.success().data(size);
    }

}
