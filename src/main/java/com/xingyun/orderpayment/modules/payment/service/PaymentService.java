package com.xingyun.orderpayment.modules.payment.service;

import com.xingyun.orderpayment.modules.payment.dto.req.PaymentCallbackReq;
import com.xingyun.orderpayment.modules.payment.dto.req.PaymentCreateReq;
import com.xingyun.orderpayment.modules.payment.dto.resp.PaymentResp;

public interface PaymentService {

    /**
     * 发起支付
     */
    PaymentResp createPayment(Long userId, PaymentCreateReq req);

    /**
     * 支付回调（幂等）
     */
    void handleCallback(PaymentCallbackReq req);

    /**
     * 查询支付状态
     */
    PaymentResp getPaymentStatus(String orderNo);

    /**
     * 定时补偿任务（扫描待支付记录，重新查询支付状态）
     */
    void compensatePendingPayments();
}