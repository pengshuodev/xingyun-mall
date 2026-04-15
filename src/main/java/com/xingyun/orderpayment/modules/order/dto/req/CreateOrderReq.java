package com.xingyun.orderpayment.modules.order.dto.req;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class CreateOrderReq {

    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemReq> items;

    private String remark;

    @Data
    public static class OrderItemReq {
        private Long productId;
        private Integer quantity;
    }
}