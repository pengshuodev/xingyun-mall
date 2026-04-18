package com.xingyun.orderpayment.modules.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyun.orderpayment.common.Result;
import com.xingyun.orderpayment.common.context.UserContext;
import com.xingyun.orderpayment.modules.order.dto.req.CreateOrderReq;
import com.xingyun.orderpayment.modules.order.dto.req.OrderListReq;
import com.xingyun.orderpayment.modules.order.dto.resp.OrderResp;
import com.xingyun.orderpayment.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Tag(name = "订单模块")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    @Operation(summary = "创建订单")
    public Result<OrderResp> createOrder(@RequestBody @Valid CreateOrderReq req) {
        Long userId = UserContext.getUserId();
        log.info("创建订单：userId={}, items={}", userId, req.getItems());
        OrderResp resp = orderService.createOrder(userId, req);
        return Result.success(resp);
    }

    @GetMapping("/list")
    @Operation(summary = "订单列表")
    public Result<Page<OrderResp>> listOrders(OrderListReq req) {
        Long userId = UserContext.getUserId();
        log.info("查询订单列表：userId={}, pageNum={}, pageSize={}, status={}",
                userId, req.getPageNum(), req.getPageSize(), req.getStatus());
        Page<OrderResp> page = orderService.listOrders(userId, req);
        return Result.success(page);
    }

    @GetMapping("/{orderNo}")
    @Operation(summary = "订单详情")
    public Result<OrderResp> getOrderDetail(@PathVariable String orderNo) {
        Long userId = UserContext.getUserId();
        log.info("查询订单详情：userId={}, orderNo={}", userId, orderNo);
        OrderResp resp = orderService.getOrderDetail(userId, orderNo);
        return Result.success(resp);
    }

    @PutMapping("/cancel/{orderNo}")
    @Operation(summary = "取消订单")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        Long userId = UserContext.getUserId();
        log.info("取消订单：userId={}, orderNo={}", userId, orderNo);
        orderService.cancelOrder(userId, orderNo);
        return Result.success();
    }
}