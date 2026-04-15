package com.xingyun.orderpayment.modules.cart.service;

import com.xingyun.orderpayment.modules.cart.dto.req.CartAddReq;
import com.xingyun.orderpayment.modules.cart.dto.req.CartUpdateReq;
import com.xingyun.orderpayment.modules.cart.dto.resp.CartItemResp;

import java.util.List;

public interface CartService {

    /**
     * 添加商品到购物车
     */
    void addToCart(Long userId, CartAddReq req);

    /**
     * 查询购物车列表
     */
    List<CartItemResp> getCart(Long userId);

    /**
     * 修改购物车商品数量
     * 如果 quantity = 0，则删除该商品
     */
    void updateQuantity(Long userId, CartUpdateReq req);

    /**
     * 删除购物车中的商品
     */
    void removeCartItem(Long userId, Long productId);

    /**
     * 清空购物车
     */
    void clearCart(Long userId);
}