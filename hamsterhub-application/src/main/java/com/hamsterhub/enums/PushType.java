package com.hamsterhub.enums;

import com.hamsterhub.common.domain.BusinessException;
import com.hamsterhub.common.domain.CommonErrorCode;
import lombok.Getter;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Getter
public enum PushType {
    GO_CQ_HTTP("GoCqHttp", "goCqPushService");

    private final String type;
    private final String beanName;

    PushType(String type, String beanName) {
        this.type = type;
        this.beanName = beanName;
    }

    public static List<String> getTypeList() {
        return Stream.of(values()).map(PushType::getType).collect(toList());
    }

    public static String getBeanNameByType(String type) {
        for (PushType pushType : values()) {
            if (pushType.getType().equals(type)) {
                return pushType.getBeanName();
            }
        }
        throw new BusinessException(CommonErrorCode.E_130001);
    }
}
