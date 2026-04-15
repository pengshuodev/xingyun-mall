package com.xingyun.orderpayment.modules.cart.dto.resp;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemResp {

    private Long productId;

    private String productName;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal totalPrice;  // price * quantity
}