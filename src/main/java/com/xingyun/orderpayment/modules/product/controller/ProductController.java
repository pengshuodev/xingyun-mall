package com.xingyun.orderpayment.modules.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyun.orderpayment.common.Result;
import com.xingyun.orderpayment.modules.product.dto.req.ProductListReq;
import com.xingyun.orderpayment.modules.product.dto.resp.ProductResp;
import com.xingyun.orderpayment.modules.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Tag(name = "商品模块")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/list")
    @Operation(summary = "商品列表（分页）")
    public Result<Page<ProductResp>> listProducts(ProductListReq req) {
        log.info("查询商品列表：keyword={}, pageNum={}, pageSize={}",
                req.getKeyword(), req.getPageNum(), req.getPageSize());
        Page<ProductResp> page = productService.listProducts(req);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "商品详情")
    public Result<ProductResp> getProductDetail(@PathVariable Long id) {
        log.info("查询商品详情：id={}", id);
        ProductResp resp = productService.getProductDetail(id);
        return Result.success(resp);
    }
}
