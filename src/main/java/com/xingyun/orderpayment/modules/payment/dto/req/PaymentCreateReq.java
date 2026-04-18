package com.xingyun.orderpayment.modules.payment.dto.req;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class PaymentCreateReq {

    @NotBlank(message = "订单号不能为空")
    private String orderNo;
}