package com.xingyun.orderpayment.modules.cart.service.impl;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.orderpayment.common.exception.BusinessException;
import com.xingyun.orderpayment.modules.cart.dto.req.CartAddReq;
import com.xingyun.orderpayment.modules.cart.dto.req.CartUpdateReq;
import com.xingyun.orderpayment.modules.cart.dto.resp.CartItemResp;
import com.xingyun.orderpayment.modules.cart.service.CartService;
import com.xingyun.orderpayment.modules.product.entity.Product;
import com.xingyun.orderpayment.modules.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final StringRedisTemplate redisTemplate;
    private final ProductMapper productMapper;

    private static final String CART_KEY_PREFIX = "cart:user:";

    /**
     * 获取购物车 Key
     */
    private String getCartKey(Long userId) {
        return CART_KEY_PREFIX + userId;
    }

    @Override
    public void addToCart(Long userId, CartAddReq req) {
        // 1. 检查商品是否存在且上架
        Product product = productMapper.selectById(req.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStatus() != 1) {
            throw new BusinessException("商品已下架");
        }

        // 2. 获取现有购物车数量
        String key = getCartKey(userId);
        String field = String.valueOf(req.getProductId());
        String existingValue = (String) redisTemplate.opsForHash().get(key, field);

        // 3. 计算最终数量
        int finalQuantity = req.getQuantity();
        if (existingValue != null) {
            CartItemResp existingItem = JSONUtil.toBean(existingValue, CartItemResp.class);
            finalQuantity += existingItem.getQuantity();
        }

        // 4. 检查库存
        if (product.getStock() < finalQuantity) {
            throw new BusinessException("库存不足，当前库存：" + product.getStock());
        }

        // 5. 构建购物车项
        CartItemResp cartItem = new CartItemResp();
        cartItem.setProductId(product.getId());
        cartItem.setProductName(product.getName());
        cartItem.setPrice(product.getPrice());
        cartItem.setQuantity(finalQuantity);
        cartItem.setTotalPrice(product.getPrice().multiply(new BigDecimal(finalQuantity)));

        // 6. 存储到 Redis
        redisTemplate.opsForHash().put(key, field, JSONUtil.toJsonStr(cartItem));
        log.info("添加购物车成功：userId={}, productId={}, finalQuantity={}", userId, req.getProductId(), finalQuantity);
    }

    @Override
    public List<CartItemResp> getCart(Long userId) {
        String key = getCartKey(userId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        ArrayList<CartItemResp> cartItems = new ArrayList<>();
        for (Object value : entries.values()) {
            CartItemResp cartItem = JSONUtil.toBean((String) value, CartItemResp.class);
            cartItems.add(cartItem);
        }

        return cartItems;
    }

    @Override
    public void updateQuantity(Long userId, CartUpdateReq req) {
        String key = getCartKey(userId);
        String field = String.valueOf(req.getProductId());

        // 1. 检查购物车中是否有该商品
        String value = (String) redisTemplate.opsForHash().get(key, field);
        if (value == null) {
            throw new BusinessException("购物车中不存在该商品");
        }

        // 2. 如果数量为0，删除该商品
        if (req.getQuantity() == 0) {
            redisTemplate.opsForHash().delete(key, field);
            log.info("移除购物车商品：userId={}, productId={}", userId, req.getProductId());
            return;
        }

        // 3. 检查库存
        Product product = productMapper.selectById(req.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getStock() < req.getQuantity()) {
            throw new BusinessException("库存不足");
        }


        // 4. 更新数量
        CartItemResp cartItem = JSONUtil.toBean(value, CartItemResp.class);
        cartItem.setQuantity(req.getQuantity());
        cartItem.setTotalPrice(cartItem.getPrice().multiply(BigDecimal.valueOf(req.getQuantity())));

        redisTemplate.opsForHash().put(key, field, JSONUtil.toJsonStr(cartItem));
        log.info("更新购物车数量：userId={}, productId={}, quantity={}", userId, req.getProductId(), req.getQuantity());
    }

    @Override
    public void removeCartItem(Long userId, Long productId) {
        String key = getCartKey(userId);
        String field = String.valueOf(productId);

        Long deletedCount = redisTemplate.opsForHash().delete(key, field);

        if (deletedCount == null || deletedCount == 0) {
            throw new BusinessException("购物车中不存在该商品");
        }

        log.info("删除购物车商品：userId={}, productId={}", userId, productId);
    }

    @Override
    public void clearCart(Long userId) {
        String key = getCartKey(userId);
        redisTemplate.delete(key);
        log.info("清空购物车：userId={}", userId);
    }
}