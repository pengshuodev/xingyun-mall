package com.xingyun.orderpayment.modules.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyun.orderpayment.common.enums.OrderStatusEnum;
import com.xingyun.orderpayment.common.enums.ProductStatusEnum;
import com.xingyun.orderpayment.common.enums.ResultCodeEnum;
import com.xingyun.orderpayment.common.exception.BusinessException;
import com.xingyun.orderpayment.modules.cart.service.CartService;
import com.xingyun.orderpayment.modules.order.dto.req.CreateOrderReq;
import com.xingyun.orderpayment.modules.order.dto.req.OrderListReq;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final CartService cartService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResp createOrder(Long userId, CreateOrderReq req) {
        // 1. 参数校验
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new BusinessException(ResultCodeEnum.ORDER_ITEM_EMPTY);
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
                throw new BusinessException(ResultCodeEnum.PRODUCT_NOT_FOUND, "商品ID：" + item.getProductId());
            }
            ProductStatusEnum status = ProductStatusEnum.fromCodeOrDefault(
                    product.getStatus(),
                    ProductStatusEnum.OFF_SALE
            );
            if (status.isOffSale()) {
                throw new BusinessException(ResultCodeEnum.PRODUCT_OFF_SALE,
                        String.format("商品[%s]已下架", product.getName()));
            }
            if (product.getStock() < item.getQuantity()) {
                throw new BusinessException(ResultCodeEnum.STOCK_INSUFFICIENT,
                        String.format("商品[%s]库存不足，当前库存：%d，需求数量：%d",
                                product.getName(), product.getStock(), item.getQuantity()));
            }
        }

        // 4. 扣减库存
        for (CreateOrderReq.OrderItemReq item : req.getItems()) {
            Product product = productMap.get(item.getProductId());
            product.setStock(product.getStock() - item.getQuantity());
            product.setUpdateTime(LocalDateTime.now());
            int updated = productMapper.updateById(product);
            if (updated == 0) {
                throw new BusinessException(ResultCodeEnum.STOCK_INSUFFICIENT,
                        String.format("商品[%s]库存已被其他用户抢购，请重新下单", product.getName()));
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
        order.setStatus(OrderStatusEnum.PENDING_PAY.getCode());
        order.setRemark(req.getRemark());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
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
            orderItem.setCreateTime(LocalDateTime.now());
            orderItems.add(orderItem);
        }
        orderItemMapper.insertBatch(orderItems);  // 需要批量插入

        log.info("订单创建成功：userId={}, orderNo={}, totalAmount={}", userId, orderNo, totalAmount);

        // 9. 构建响应
        OrderResp resp = new OrderResp();
        resp.setOrderNo(orderNo);
        resp.setUserId(userId);
        resp.setTotalAmount(totalAmount);
        resp.setStatus(OrderStatusEnum.PENDING_PAY.getCode());
        resp.setStatusDesc(OrderStatusEnum.PENDING_PAY.getDesc());
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

        // 10. 清空购物车中已下单的商品
        try {
            for (CreateOrderReq.OrderItemReq item : req.getItems()) {
                cartService.removeCartItem(userId, item.getProductId());
            }
            log.info("下单成功，已清空购物车：userId={}, orderNo={}", userId, orderNo);
        } catch (Exception e) {
            log.warn("清空购物车失败，不影响订单：userId={}, orderNo={}", userId, orderNo, e);
        }

        return resp;
    }

    @Override
    public Page<OrderResp> listOrders(Long userId, OrderListReq req) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        wrapper.eq(Order::getIsDeleted, 0);
        if (req.getStatus() != null) {
            OrderStatusEnum.fromCode(req.getStatus())
                    .orElseThrow(() -> new BusinessException(ResultCodeEnum.BAD_REQUEST, "无效的订单状态"));
            wrapper.eq(Order::getStatus, req.getStatus());
        }
        wrapper.orderByDesc(Order::getCreateTime);

        // 2. 分页查询订单主表
        Page<Order> page = new Page<>(req.getPageNum(), req.getPageSize());
        Page<Order> orderPage = orderMapper.selectPage(page, wrapper);

        // 3. 查询每个订单的明细
        ArrayList<OrderResp> orderRespList = new ArrayList<>();
        for (Order record : orderPage.getRecords()) {
            OrderResp orderResp = convertToOrderResp(record);
            orderRespList.add(orderResp);
        }

        // 4. 构建分页响应
        Page<OrderResp> respPage = new Page<>(orderPage.getCurrent(), orderPage.getSize());
        respPage.setTotal(orderPage.getTotal());
        respPage.setRecords(orderRespList);

        return respPage;
    }

    @Override
    public OrderResp getOrderDetail(Long userId, String orderNo) {
        // 1. 查询订单
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, orderNo);
        wrapper.eq(Order::getUserId, userId);
        wrapper.eq(Order::getIsDeleted, 0);
        Order order = orderMapper.selectOne(wrapper);

        if (order == null) {
            throw new BusinessException(ResultCodeEnum.ORDER_NOT_FOUND);
        }

        // 2. 转换并返回
        return convertToOrderResp(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, String orderNo) {
        // 1. 查询并校验订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId);
        Order order = orderMapper.selectOne(orderWrapper);

        if (order == null) {
            throw new BusinessException(ResultCodeEnum.ORDER_NOT_FOUND);
        }
        OrderStatusEnum currentStatus = OrderStatusEnum.fromCodeOrDefault(
                order.getStatus(),
                OrderStatusEnum.PENDING_PAY
        );

        if (!currentStatus.canCancel()) {
            throw new BusinessException(ResultCodeEnum.ORDER_STATUS_ERROR,
                    String.format("订单状态不允许取消，当前状态：%s", currentStatus.getDesc()));
        }
        // 2. 查询订单明细
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderNo, orderNo);
        List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);

        if (orderItems == null || orderItems.isEmpty()) {
            throw new BusinessException(ResultCodeEnum.ORDER_ITEM_EMPTY);
        }

        // 3. 批量查询商品（性能优化）
        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        // 关键：构建 Map 保证对应关系准确
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 4. 恢复库存（处理乐观锁）
        for (OrderItem item : orderItems) {
            Product product = productMap.get(item.getProductId());

            // 严格校验商品存在性
            if (product == null) {
                log.error("订单取消异常：商品不存在，productId={}, orderNo={}", item.getProductId(), orderNo);
                throw new BusinessException(ResultCodeEnum.PRODUCT_NOT_FOUND,
                        "商品ID：" + item.getProductId());
            }

            // 计算新库存并更新
            product.setStock(product.getStock() + item.getQuantity());
            product.setUpdateTime(LocalDateTime.now());

            int updated = productMapper.updateById(product);

            // 乐观锁冲突处理
            if (updated == 0) {
                // 重新查询当前库存
                Product currentProduct = productMapper.selectById(product.getId());
                log.warn("库存更新冲突，productId={}, 期望stock={}, 实际stock={}",
                        product.getId(), product.getStock() - item.getQuantity(), currentProduct.getStock());
                throw new BusinessException(ResultCodeEnum.INTERNAL_ERROR, "操作频繁，请稍后重试");
            }

            log.info("恢复库存成功：productId={}, quantity={}, newStock={}",
                    item.getProductId(), item.getQuantity(), product.getStock());
        }

        // 5. 更新订单状态
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单取消成功：orderNo={}, userId={}", orderNo, userId);
    }

    @Override
    public void closeTimeoutOrders() {
        // 1. 查询超时未支付的订单（30分钟）
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, OrderStatusEnum.PENDING_PAY.getCode())
                .lt(Order::getCreateTime, LocalDateTime.now().minusMinutes(30));
        List<Order> timeoutOrders = orderMapper.selectList(wrapper);

        if (timeoutOrders.isEmpty()) {
            log.debug("无超时订单");
            return;
        }

        log.info("发现 {} 个超时订单，开始自动关单", timeoutOrders.size());

        int successCount = 0;
        int failCount = 0;

        for (Order order : timeoutOrders) {
            try {
                closeSingleOrder(order);
                successCount++;
                log.info("超时关单成功：orderNo={}", order.getOrderNo());
            } catch (Exception e) {
                failCount++;
                log.error("超时关单失败：orderNo={}", order.getOrderNo(), e);
            }
        }

        log.info("超时关单任务完成：成功={}, 失败={}", successCount, failCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeSingleOrder(Order order) {
        // 二次检查：防止并发（订单可能已被其他操作处理）
        Order latestOrder = orderMapper.selectById(order.getId());
        if (latestOrder == null) {
            log.error("订单不存在：orderNo={}", order.getOrderNo());
            throw new BusinessException(ResultCodeEnum.ORDER_NOT_FOUND);
        }
        // 使用枚举判断状态
        OrderStatusEnum currentStatus = OrderStatusEnum.fromCodeOrDefault(
                latestOrder.getStatus(),
                OrderStatusEnum.PENDING_PAY
        );

        // 只有待支付状态才能超时关闭
        if (!currentStatus.canCancel()) {
            log.info("订单已被处理，跳过：orderNo={}, currentStatus={}",
                    order.getOrderNo(), currentStatus.getDesc());
            return;
        }

        // 1. 查询订单明细
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderNo, latestOrder.getOrderNo());
        List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);

        if (orderItems == null || orderItems.isEmpty()) {
            log.error("订单明细为空：orderNo={}", latestOrder.getOrderNo());
            throw new BusinessException(ResultCodeEnum.ORDER_ITEM_EMPTY);
        }

        // 2. 批量查询商品
        List<Long> productIds = orderItems.stream()
                .map(OrderItem::getProductId)
                .distinct()
                .toList();
        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (e, r) -> e));

        // 3. 恢复库存
        for (OrderItem item : orderItems) {
            Product product = productMap.get(item.getProductId());
            if (product == null) {
                log.error("商品不存在：productId={}, orderNo={}",
                        item.getProductId(), latestOrder.getOrderNo());
                throw new BusinessException(ResultCodeEnum.PRODUCT_NOT_FOUND);
            }

            product.setStock(product.getStock() + item.getQuantity());
            product.setUpdateTime(LocalDateTime.now());

            int updated = productMapper.updateById(product);
            if (updated == 0) {
                log.warn("库存恢复冲突，productId={}, version={}",
                        product.getId(), product.getVersion());
                throw new BusinessException(ResultCodeEnum.INTERNAL_ERROR, "库存恢复失败，请重试");
            }

            log.debug("恢复库存：productId={}, quantity={}, newStock={}",
                    item.getProductId(), item.getQuantity(), product.getStock());
        }

        // 4. 更新订单状态（3-超时关闭）
        latestOrder.setStatus(OrderStatusEnum.CLOSED.getCode());
        latestOrder.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(latestOrder);

        log.info("超时关单完成：orderNo={}", latestOrder.getOrderNo());
    }

    /**
     * 订单实体转响应 DTO
     */
    private OrderResp convertToOrderResp(Order order) {
        OrderResp resp = new OrderResp();
        resp.setOrderNo(order.getOrderNo());
        resp.setUserId(order.getUserId());
        resp.setTotalAmount(order.getTotalAmount());
        resp.setStatus(order.getStatus());
        resp.setStatusDesc(OrderStatusEnum.getDescByCode(order.getStatus()));
        resp.setCreateTime(order.getCreateTime());


        // 查询订单明细
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderNo, order.getOrderNo());
        List<OrderItem> orderItems = orderItemMapper.selectList(itemWrapper);

        List<OrderResp.OrderItemResp> itemResps = new ArrayList<>();
        for (OrderItem item : orderItems) {
            OrderResp.OrderItemResp itemResp = new OrderResp.OrderItemResp();
            itemResp.setProductId(item.getProductId());
            itemResp.setProductName(item.getProductName());
            itemResp.setPrice(item.getPrice());
            itemResp.setQuantity(item.getQuantity());
            itemResp.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            itemResps.add(itemResp);
        }
        resp.setItems(itemResps);

        return resp;
    }

}
