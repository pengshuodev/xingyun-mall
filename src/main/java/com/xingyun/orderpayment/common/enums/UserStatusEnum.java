package com.xingyun.orderpayment.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public enum UserStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer code;
    private final String desc;

    UserStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static final Map<Integer, UserStatusEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(
                    UserStatusEnum::getCode,
                    e -> e
            ));

    public static Optional<UserStatusEnum> fromCode(Integer code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    public static UserStatusEnum fromCodeOrDefault(Integer code, UserStatusEnum defaultStatus) {
        return CODE_MAP.getOrDefault(code, defaultStatus);
    }

    public static String getDescByCode(Integer code) {
        return fromCode(code)
                .map(UserStatusEnum::getDesc)
                .orElse("未知状态[" + code + "]");
    }

    public boolean isEnabled() {
        return this == ENABLED;
    }

    public boolean isDisabled() {
        return this == DISABLED;
    }
}