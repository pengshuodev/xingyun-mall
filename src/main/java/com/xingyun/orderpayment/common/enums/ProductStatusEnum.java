package com.xingyun.orderpayment.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public enum ProductStatusEnum {

    OFF_SALE(0, "下架"),
    ON_SALE(1, "上架");

    private final Integer code;
    private final String desc;

    ProductStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 缓存 Map
    private static final Map<Integer, ProductStatusEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(
                    ProductStatusEnum::getCode,
                    e -> e
            ));

    /**
     * 根据 code 获取枚举（返回 Optional，避免 NPE）
     */
    public static Optional<ProductStatusEnum> fromCode(Integer code) {
        return Optional.ofNullable(CODE_MAP.get(code));
    }

    /**
     * 根据 code 获取枚举，不存在时返回默认值
     */
    public static ProductStatusEnum fromCodeOrDefault(Integer code, ProductStatusEnum defaultStatus) {
        return CODE_MAP.getOrDefault(code, defaultStatus);
    }

    /**
     * 根据 code 获取描述
     */
    public static String getDescByCode(Integer code) {
        return fromCode(code)
                .map(ProductStatusEnum::getDesc)
                .orElse("未知状态[" + code + "]");
    }

    /**
     * 判断是否上架
     */
    public boolean isOnSale() {
        return this == ON_SALE;
    }

    /**
     * 判断是否下架
     */
    public boolean isOffSale() {
        return this == OFF_SALE;
    }
}