package com.xingyun.orderpayment.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public enum OrderStatusEnum {

    PENDING_PAY(0, "待支付"),
    PAID(1, "已支付"),
    CANCELLED(2, "已取消"),
    CLOSED(3, "已关闭");

    private final Integer code;
    private final String desc;

    OrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 缓存 Map
    private static final Map<Integer, OrderStatusEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(
                    OrderStatusEnum::getCode,
                    e -> e
            ));

    /**
     * 根据 code 获取枚举（返回 Optional，避免 NPE）
     */
    public static Optional<OrderStatusEnum> fromCode(Integer code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    /**
     * 根据 code 获取枚举，不存在时返回默认值
     */
    public static OrderStatusEnum fromCodeOrDefault(Integer code, OrderStatusEnum defaultStatus) {
        return CODE_MAP.getOrDefault(code, defaultStatus);
    }

    /**
     * 根据 code 获取描述
     */
    public static String getDescByCode(Integer code) {
        return fromCode(code)
                .map(OrderStatusEnum::getDesc)
                .orElse("未知状态[" + code + "]");
    }

    /**
     * 判断是否为终态（不可再修改）
     */
    public boolean isFinalStatus() {
        return this == CANCELLED || this == CLOSED;
    }

    /**
     * 判断是否可支付
     */
    public boolean canPay() {
        return this == PENDING_PAY;
    }

    /**
     * 判断是否可取消
     */
    public boolean canCancel() {
        return this == PENDING_PAY;
    }
}