package com.xingyun.orderpayment.modules.payment.controller;

import com.xingyun.orderpayment.common.Result;
import com.xingyun.orderpayment.common.context.UserContext;
import com.xingyun.orderpayment.modules.payment.dto.req.PaymentCallbackReq;
import com.xingyun.orderpayment.modules.payment.dto.req.PaymentCreateReq;
import com.xingyun.orderpayment.modules.payment.dto.resp.PaymentResp;
import com.xingyun.orderpayment.modules.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = "支付模块")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    @Operation(summary = "发起支付")
    public Result<PaymentResp> createPayment(@RequestBody @Valid PaymentCreateReq req) {
        Long userId = UserContext.getUserId();
        log.info("发起支付：userId={}, orderNo={}", userId, req.getOrderNo());
        PaymentResp resp = paymentService.createPayment(userId, req);
        return Result.success(resp);
    }

    @PostMapping("/callback")
    @Operation(summary = "支付回调（模拟）")
    public Result<Void> callback(@RequestBody PaymentCallbackReq req) {
        log.info("支付回调：paymentNo={}, orderNo={}, status={}", req.getPaymentNo(), req.getOrderNo(), req.getStatus());
        paymentService.handleCallback(req);
        return Result.success();
    }

    @GetMapping("/status/{orderNo}")
    @Operation(summary = "查询支付状态")
    public Result<PaymentResp> getPaymentStatus(@PathVariable String orderNo) {
        Long userId = UserContext.getUserId();
        log.info("查询支付状态：userId={}, orderNo={}", userId, orderNo);
        PaymentResp resp = paymentService.getPaymentStatus(orderNo);
        return Result.success(resp);
    }

    @GetMapping("/mock-pay")
    @Operation(summary = "模拟支付页面")
    public String mockPayPage(@RequestParam String paymentNo, @RequestParam String orderNo) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>模拟支付</title>
                <style>
                    body { font-family: Arial; text-align: center; padding: 50px; }
                    button { font-size: 18px; padding: 10px 30px; background: #4CAF50; color: white; border: none; border-radius: 5px; cursor: pointer; }
                    button:hover { background: #45a049; }
                    .info { margin-bottom: 30px; font-size: 16px; color: #666; }
                </style>
            </head>
            <body>
                <h2>模拟支付</h2>
                <div class="info">支付单号：%s</div>
                <div class="info">订单号：%s</div>
                <button onclick="pay()">点击支付成功</button>
                <script>
                    async function pay() {
                        // 调用支付回调
                        const response = await fetch('/api/payment/callback', {
                            method: 'POST',
                            headers: {'Content-Type': 'application/json'},
                            body: JSON.stringify({
                                paymentNo: '%s',
                                orderNo: '%s',
                                status: 1,
                                callbackData: 'mock payment success'
                            })
                        });
                        const result = await response.json();
                        if (result.code === 200) {
                            alert('支付成功！');
                            window.location.href = '/swagger-ui/index.html';
                        } else {
                            alert('支付失败：' + result.message);
                        }
                    }
                </script>
            </body>
            </html>
            """.formatted(paymentNo, orderNo, paymentNo, orderNo);
    }
}