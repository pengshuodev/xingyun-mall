package com.xingyun.orderpayment.modules.product.dto.req;

import lombok.Data;

@Data
public class ProductListReq {

    private String keyword;  // 搜索关键词（商品名称）

    private Integer pageNum = 1;  // 页码，默认1

    private Integer pageSize = 10;  // 每页条数，默认10
}