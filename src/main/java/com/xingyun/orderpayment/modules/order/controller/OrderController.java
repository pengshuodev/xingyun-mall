package com.xingyun.orderpayment.modules.order.controller;

import com.xingyun.orderpayment.common.Result;
import com.xingyun.orderpayment.common.context.UserContext;
import com.xingyun.orderpayment.modules.order.dto.req.CreateOrderReq;
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
}