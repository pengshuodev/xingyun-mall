package com.xingyun.orderpayment.modules.order.dto.resp;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResp {

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    private Integer status;

    private String statusDesc;

    private LocalDateTime createTime;

    private List<OrderItemResp> items;

    @Data
    public static class OrderItemResp {
        private Long productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal totalPrice;
    }
}