package com.xingyun.orderpayment.modules.payment.dto.resp;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentResp {

    private String paymentNo;

    private String orderNo;

    private BigDecimal amount;

    private Integer status;

    private String statusDesc;

    private String payUrl;  // 模拟支付链接
}