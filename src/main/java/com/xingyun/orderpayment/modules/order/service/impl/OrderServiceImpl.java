package com.xingyun.orderpayment.modules.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.xingyun.orderpayment.common.exception.BusinessException;
import com.xingyun.orderpayment.modules.order.dto.req.CreateOrderReq;
import com.xingyun.orderpayment.modules.order.dto.resp.OrderResp;
import com.xingyun.orderpayment.modules.order.entity.Order;
import com.xingyun.orderpayment.modules.order.entity.OrderItem;
import com.xingyun.orderpayment.modules.order.mapper.OrderItemMapper;
import com.xingyun.orderpayment.modules.order.mapper.OrderMapper;
import com.xingyun.orderpayment.modules.order.service.OrderService;
import com.xingyun.orderpayment.modules.product.entity.Product;
import com.xingyun.orderpayment.modules.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResp createOrder(Long userId, CreateOrderReq req) {
        // 1. 参数校验
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new RuntimeException("订单项不能为空");
        }

        // 2. 获取商品信息
        List<Long> productIds = req.getItems().stream()
                .map(CreateOrderReq.OrderItemReq::getProductId)
                .toList();
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        // 3. 校验商品是否存在、库存是否充足
        for (CreateOrderReq.OrderItemReq item : req.getItems()) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在：" + item.getProductId());
            }
            if (product.getStatus() != 1) {
                throw new BusinessException("商品已下架：" + product.getName());
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException("库存不足：" + product.getName() + "，当前库存：" + product.getStock());
            }
        }

        // 4. 扣减库存
        for (CreateOrderReq.OrderItemReq item : req.getItems()) {
            Product product = productMap.get(item.getProductId());
            product.setStock(product.getStock() - item.getQuantity());
            int updated = productMapper.updateById(product);
            if (updated == 0) {
                throw new BusinessException("下单失败，请重试");
            }
        }

        // 5. 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateOrderReq.OrderItemReq item : req.getItems()) {
            Product product = productMap.get(item.getProductId());
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
        }

        // 6. 生成订单号（雪花算法）
        String orderNo = IdUtil.getSnowflakeNextIdStr();

        // 7. 创建订单主表
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);// 0-待支付
        order.setRemark(req.getRemark());
        orderMapper.insert(order);

        // 8. 创建订单明细
        List<OrderItem> orderItems = new ArrayList<>();
        for (CreateOrderReq.OrderItemReq item : req.getItems()) {
            Product product = productMap.get(item.getProductId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderNo(orderNo);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItems.add(orderItem);
        }
        orderItemMapper.insertBatch(orderItems);  // 需要批量插入

        log.info("订单创建成功：userId={}, orderNo={}, totalAmount={}", userId, orderNo, totalAmount);

        // 9. 构建响应
        OrderResp resp = new OrderResp();
        resp.setOrderNo(orderNo);
        resp.setUserId(userId);
        resp.setTotalAmount(totalAmount);
        resp.setStatus(0);
        resp.setStatusDesc("待支付");
        resp.setCreateTime(order.getCreateTime());

        List<OrderResp.OrderItemResp> itemResps = new ArrayList<>();
        for (CreateOrderReq.OrderItemReq item : req.getItems()) {
            Product product = productMap.get(item.getProductId());
            OrderResp.OrderItemResp itemResp = new OrderResp.OrderItemResp();
            itemResp.setProductId(product.getId());
            itemResp.setProductName(product.getName());
            itemResp.setPrice(product.getPrice());
            itemResp.setQuantity(item.getQuantity());
            itemResp.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            itemResps.add(itemResp);
        }
        resp.setItems(itemResps);

        return resp;
    }
}
