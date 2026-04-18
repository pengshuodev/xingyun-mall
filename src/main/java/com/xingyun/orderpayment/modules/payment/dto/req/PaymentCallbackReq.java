package com.xingyun.orderpayment.modules.payment.dto.req;

import lombok.Data;

@Data
public class PaymentCallbackReq {

    private String paymentNo;   // 支付流水号

    private String orderNo;     // 订单号

    private Integer status;     // 1-成功 2-失败

    private String callbackData; // 原始回调数据
}