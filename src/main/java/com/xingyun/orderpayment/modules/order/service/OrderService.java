package com.xingyun.orderpayment.modules.order.service;

import com.xingyun.orderpayment.modules.order.dto.req.CreateOrderReq;
import com.xingyun.orderpayment.modules.order.dto.resp.OrderResp;

public interface OrderService {

    /**
     * 创建订单
     */
    OrderResp createOrder(Long userId, CreateOrderReq req);
}