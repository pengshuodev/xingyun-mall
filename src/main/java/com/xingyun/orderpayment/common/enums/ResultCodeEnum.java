package com.xingyun.orderpayment.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ResultCodeEnum {

    SUCCESS(200, "成功"),

    // ==================== 通用错误 1000-1999 ====================
    BAD_REQUEST(1000, "请求参数错误"),
    UNAUTHORIZED(1001, "未登录"),
    FORBIDDEN(1002, "无权限"),
    NOT_FOUND(1003, "资源不存在"),
    INTERNAL_ERROR(1004, "系统内部错误"),

    // ==================== 用户模块 2000-2999 ====================
    USER_NOT_FOUND(2000, "用户不存在"),
    USERNAME_EXISTS(2001, "用户名已存在"),
    PHONE_EXISTS(2002, "手机号已注册"),
    PASSWORD_ERROR(2003, "用户名或密码错误"),
    USER_DISABLED(2004, "账号已被禁用"),

    // ==================== 商品模块 3000-3999 ====================
    PRODUCT_NOT_FOUND(3000, "商品不存在"),
    PRODUCT_OFF_SALE(3001, "商品已下架"),
    STOCK_INSUFFICIENT(3002, "库存不足"),

    // ==================== 订单模块 4000-4999 ====================
    ORDER_NOT_FOUND(4000, "订单不存在"),
    ORDER_STATUS_ERROR(4001, "订单状态不允许操作"),
    ORDER_ITEM_EMPTY(4002, "订单明细为空"),

    // ==================== 支付模块 5000-5999 ====================
    PAYMENT_NOT_FOUND(5000, "支付记录不存在"),
    PAYMENT_STATUS_ERROR(5001, "支付状态不正确"),
    PAYMENT_CALLBACK_DUPLICATE(5002, "重复回调"),

    // ==================== 购物车模块 6000-6999 ====================
    CART_ITEM_NOT_FOUND(6000, "购物车中不存在该商品");

    private final Integer code;
    private final String message;

    // 缓存所有枚举值，提高查找效率
    private static final Map<Integer, ResultCodeEnum> CODE_MAP =
            Arrays.stream(values()).collect(Collectors.toMap(ResultCodeEnum::getCode, e -> e));

    /**
     * 根据错误码获取枚举
     */
    public static ResultCodeEnum of(Integer code) {
        return CODE_MAP.get(code);
    }

    /**
     * 判断是否为成功
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
}