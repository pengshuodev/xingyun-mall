package com.xingyun.orderpayment.modules.cart.dto.req;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
public class CartUpdateReq {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "数量不能为空")
    @PositiveOrZero(message = "数量必须大于等于0")
    private Integer quantity;
}