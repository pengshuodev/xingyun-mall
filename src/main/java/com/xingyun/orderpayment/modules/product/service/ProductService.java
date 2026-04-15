package com.xingyun.orderpayment.modules.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyun.orderpayment.modules.product.dto.req.ProductListReq;
import com.xingyun.orderpayment.modules.product.dto.resp.ProductResp;

public interface ProductService {

    /**
     * 分页查询商品列表
     */
    Page<ProductResp> listProducts(ProductListReq req);

    /**
     * 查询商品详情
     */
    ProductResp getProductDetail(Long id);
}