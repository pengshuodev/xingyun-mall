package com.xingyun.orderpayment.modules.cart.controller;

import com.xingyun.orderpayment.common.Result;
import com.xingyun.orderpayment.common.context.UserContext;
import com.xingyun.orderpayment.modules.cart.dto.req.CartAddReq;
import com.xingyun.orderpayment.modules.cart.dto.req.CartUpdateReq;
import com.xingyun.orderpayment.modules.cart.dto.resp.CartItemResp;
import com.xingyun.orderpayment.modules.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "购物车模块")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    @Operation(summary = "添加商品到购物车")
    public Result<Void> addToCart(@RequestBody @Valid CartAddReq req) {
        Long userId = UserContext.getUserId();
        log.info("添加购物车：userId={}, productId={}, quantity={}",
                userId, req.getProductId(), req.getQuantity());
        cartService.addToCart(userId, req);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "查询购物车列表")
    public Result<List<CartItemResp>> getCart() {
        Long userId = UserContext.getUserId();
        log.info("查询购物车：userId={}", userId);
        List<CartItemResp> cartItems = cartService.getCart(userId);
        return Result.success(cartItems);
    }

    @PutMapping("/update")
    @Operation(summary = "修改购物车商品数量")
    public Result<Void> updateQuantity(@RequestBody @Valid CartUpdateReq req) {
        Long userId = UserContext.getUserId();
        log.info("修改购物车数量：userId={}, productId={}, quantity={}",
                userId, req.getProductId(), req.getQuantity());
        cartService.updateQuantity(userId, req);
        return Result.success();
    }

    @DeleteMapping("/remove/{productId}")
    @Operation(summary = "删除购物车中的商品")
    public Result<Void> removeCartItem(@PathVariable Long productId) {
        Long userId = UserContext.getUserId();
        log.info("删除购物车商品：userId={}, productId={}", userId, productId);
        cartService.removeCartItem(userId, productId);
        return Result.success();
    }

    @DeleteMapping("/clear")
    @Operation(summary = "清空购物车")
    public Result<Void> clearCart() {
        Long userId = UserContext.getUserId();
        log.info("清空购物车：userId={}", userId);
        cartService.clearCart(userId);
        return Result.success();
    }
}