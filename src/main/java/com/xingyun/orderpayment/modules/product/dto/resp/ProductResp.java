package com.xingyun.orderpayment.modules.product.dto.resp;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductResp {

    private Long id;

    private String name;

    private BigDecimal price;

    private Integer stock;

    private String description;

    private Integer status;

    private String imageUrl;
}