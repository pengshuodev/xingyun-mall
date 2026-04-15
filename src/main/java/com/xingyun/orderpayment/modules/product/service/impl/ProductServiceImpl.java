package com.xingyun.orderpayment.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyun.orderpayment.common.exception.BusinessException;
import com.xingyun.orderpayment.modules.product.dto.req.ProductListReq;
import com.xingyun.orderpayment.modules.product.dto.resp.ProductResp;
import com.xingyun.orderpayment.modules.product.entity.Product;
import com.xingyun.orderpayment.modules.product.mapper.ProductMapper;
import com.xingyun.orderpayment.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Page<ProductResp> listProducts(ProductListReq req) {
        // 1.构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 按商品名称模糊查询
        if (req.getKeyword() != null && !req.getKeyword().isEmpty()) {
            wrapper.like(Product::getName, req.getKeyword());
        }
        // 只查询上架的商品
        wrapper.eq(Product::getStatus, 1);
        // 按创建时间倒序排序
        wrapper.orderByDesc(Product::getCreateTime);

        // 2.分页查询
        Page<Product> page = new Page<>(req.getPageNum(), req.getPageSize());
        Page<Product> productPage = productMapper.selectPage(page, wrapper);

        // 3.转换为响应 DTO
        Page<ProductResp> respPage = new Page<>(productPage.getCurrent(), productPage.getSize());
        respPage.setTotal(productPage.getTotal());
        respPage.setRecords(productPage.getRecords().stream().map(this::convertToResp).toList());

        return respPage;
    }

    @Override
    public ProductResp getProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        if(product == null){
            throw new BusinessException("商品不存在");
        }
        if(product.getStatus() != 1){
            throw new BusinessException("商品已下架");
        }
        return convertToResp(product);
    }

    /**
     * Entity 转 Resp
     */
    private ProductResp convertToResp(Product product) {
        ProductResp resp = new ProductResp();
        BeanUtils.copyProperties(product, resp);
        return resp;
    }
}
