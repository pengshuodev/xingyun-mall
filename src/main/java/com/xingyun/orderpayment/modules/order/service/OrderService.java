package com.xingyun.orderpayment.modules.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyun.orderpayment.modules.order.dto.req.CreateOrderReq;
import com.xingyun.orderpayment.modules.order.dto.req.OrderListReq;
import com.xingyun.orderpayment.modules.order.dto.resp.OrderResp;

public interface OrderService {

    /**
     * 创建订单
     */
    OrderResp createOrder(Long userId, CreateOrderReq req);

    /**
     * 分页查询用户订单列表
     */
    Page<OrderResp> listOrders(Long userId, OrderListReq req);

    /**
     * 查询订单详情
     */
    OrderResp getOrderDetail(Long userId, String orderNo);
}