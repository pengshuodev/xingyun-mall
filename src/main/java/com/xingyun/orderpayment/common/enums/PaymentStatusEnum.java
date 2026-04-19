package com.xingyun.orderpayment.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public enum PaymentStatusEnum {

    PENDING(0, "待支付"),
    SUCCESS(1, "支付成功"),
    FAILED(2, "支付失败");

    private final Integer code;
    private final String desc;

    PaymentStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 缓存 Map
    private static final Map<Integer, PaymentStatusEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(
                    PaymentStatusEnum::getCode,
                    e -> e
            ));

    /**
     * 根据 code 获取枚举（返回 Optional，避免 NPE）
     */
    public static Optional<PaymentStatusEnum> fromCode(Integer code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    /**
     * 根据 code 获取枚举，不存在时返回默认值
     */
    public static PaymentStatusEnum fromCodeOrDefault(Integer code, PaymentStatusEnum defaultStatus) {
        return CODE_MAP.getOrDefault(code, defaultStatus);
    }

    /**
     * 根据 code 获取描述
     */
    public static String getDescByCode(Integer code) {
        return fromCode(code)
                .map(PaymentStatusEnum::getDesc)
                .orElse("未知状态[" + code + "]");
    }

    /**
     * 判断是否支付成功
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 判断是否支付失败
     */
    public boolean isFailed() {
        return this == FAILED;
    }

    /**
     * 判断是否待支付
     */
    public boolean isPending() {
        return this == PENDING;
    }

    /**
     * 判断是否可重试（待支付或失败状态可重试）
     */
    public boolean canRetry() {
        return this == PENDING || this == FAILED;
    }

    /**
     * 判断是否为终态
     */
    public boolean isFinalStatus() {
        return this == SUCCESS || this == FAILED;
    }
}