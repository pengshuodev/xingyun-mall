package com.xingyun.orderpayment.modules.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.orderpayment.common.exception.BusinessException;
import com.xingyun.orderpayment.modules.order.entity.Order;
import com.xingyun.orderpayment.modules.order.mapper.OrderMapper;
import com.xingyun.orderpayment.modules.payment.dto.req.PaymentCallbackReq;
import com.xingyun.orderpayment.modules.payment.dto.req.PaymentCreateReq;
import com.xingyun.orderpayment.modules.payment.dto.resp.PaymentResp;
import com.xingyun.orderpayment.modules.payment.entity.PaymentLog;
import com.xingyun.orderpayment.modules.payment.mapper.PaymentLogMapper;
import com.xingyun.orderpayment.modules.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentLogMapper paymentLogMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentResp createPayment(Long userId, PaymentCreateReq req) {
        // 1. 查询订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getOrderNo, req.getOrderNo());
        orderWrapper.eq(Order::getUserId, userId);
        Order order = orderMapper.selectOne(orderWrapper);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态异常");
        }

        // 2. 检查是否已有支付记录
        LambdaQueryWrapper<PaymentLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(PaymentLog::getOrderNo, req.getOrderNo());
        PaymentLog existingLog = paymentLogMapper.selectOne(logWrapper);
        if (existingLog != null) {
            // 已有支付记录，返回已有信息
            PaymentResp resp = new PaymentResp();
            resp.setPaymentNo(existingLog.getPaymentNo());
            resp.setOrderNo(existingLog.getOrderNo());
            resp.setAmount(existingLog.getAmount());
            resp.setStatus(existingLog.getStatus());
            resp.setStatusDesc(getStatusDesc(existingLog.getStatus()));
            resp.setPayUrl("http://localhost:8089/api/payment/mock-pay?paymentNo=" + existingLog.getPaymentNo());
            return resp;
        }

        // 3. 创建支付记录
        String paymentNo = IdUtil.getSnowflakeNextIdStr();
        PaymentLog paymentLog = new PaymentLog();
        paymentLog.setPaymentNo(paymentNo);
        paymentLog.setOrderNo(req.getOrderNo());
        paymentLog.setAmount(order.getTotalAmount());
        paymentLog.setStatus(0);  // 待支付
        paymentLog.setRetryCount(0);
        paymentLog.setCreateTime(LocalDateTime.now());
        paymentLog.setUpdateTime(LocalDateTime.now());
        paymentLogMapper.insert(paymentLog);

        // 4. 返回支付信息
        PaymentResp resp = new PaymentResp();
        resp.setPaymentNo(paymentNo);
        resp.setOrderNo(req.getOrderNo());
        resp.setAmount(order.getTotalAmount());
        resp.setStatus(0);
        resp.setStatusDesc("待支付");
        resp.setPayUrl("http://localhost:8089/api/payment/mock-pay?paymentNo=" + paymentNo + "&orderNo=" + req.getOrderNo());

        log.info("创建支付单成功：userId={}, orderNo={}, paymentNo={}", userId, req.getOrderNo(), paymentNo);
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleCallback(PaymentCallbackReq req) {
        log.info("收到支付回调：paymentNo={}, orderNo={}, status={}",
                req.getPaymentNo(), req.getOrderNo(), req.getStatus());

        // 1. 查询支付记录
        LambdaQueryWrapper<PaymentLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentLog::getPaymentNo, req.getPaymentNo());
        PaymentLog paymentLog = paymentLogMapper.selectOne(wrapper);

        if (paymentLog == null) {
            log.warn("支付记录不存在：paymentNo={}", req.getPaymentNo());
            throw new BusinessException("支付记录不存在");
        }

        // 2. 幂等性检查
        if (paymentLog.getStatus() != 0) {
            log.info("支付记录已处理，忽略重复回调：paymentNo={}, currentStatus={}",
                    req.getPaymentNo(), paymentLog.getStatus());
            return;
        }

        // 3. 更新支付记录
        paymentLog.setStatus(req.getStatus());
        paymentLog.setCallbackData(req.getCallbackData());
        paymentLog.setUpdateTime(LocalDateTime.now());
        paymentLogMapper.updateById(paymentLog);

        // 4. 支付成功后更新订单
        if (req.getStatus() == 1) {
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getOrderNo, req.getOrderNo());
            Order order = orderMapper.selectOne(orderWrapper);

            if (order == null) {
                log.error("支付成功但订单不存在：orderNo={}, paymentNo={}",
                        req.getOrderNo(), req.getPaymentNo());
                // 订单不存在，需要人工介入或异步补偿
                // 可以发送告警或写入异常表
                return;
            }

            if (order.getStatus() != 0) {
                log.warn("订单状态非待支付，跳过更新：orderNo={}, currentStatus={}, paymentNo={}",
                        req.getOrderNo(), order.getStatus(), req.getPaymentNo());
                return;
            }

            order.setStatus(1);
            order.setPaymentTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            log.info("订单支付成功：orderNo={}, paymentNo={}", req.getOrderNo(), req.getPaymentNo());
        }

        log.info("支付回调处理完成：paymentNo={}, orderStatus={}", req.getPaymentNo(), req.getStatus());
    }

    @Override
    public PaymentResp getPaymentStatus(String orderNo) {
        LambdaQueryWrapper<PaymentLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentLog::getOrderNo, orderNo);
        PaymentLog paymentLog = paymentLogMapper.selectOne(wrapper);

        if (paymentLog == null) {
            throw new BusinessException("支付记录不存在");
        }

        PaymentResp resp = new PaymentResp();
        resp.setPaymentNo(paymentLog.getPaymentNo());
        resp.setOrderNo(paymentLog.getOrderNo());
        resp.setAmount(paymentLog.getAmount());
        resp.setStatus(paymentLog.getStatus());
        resp.setStatusDesc(getStatusDesc(paymentLog.getStatus()));

        return resp;
    }

    @Override
    public void compensatePendingPayments() {
        log.info("开始执行支付补偿任务");

        // 1. 查询待补偿的支付记录
        List<PaymentLog> pendingLogs = paymentLogMapper.selectPendingCompensation();
        if(pendingLogs == null || pendingLogs.isEmpty()){
            log.info("无需补偿的支付记录");
            return;
        }

        log.info("发现 {} 条待补偿的支付记录", pendingLogs.size());

        // 2. 遍历处理
        for (PaymentLog logItem : pendingLogs) {
            try {
                paymentLogMapper.incrementRetryCount(logItem.getPaymentNo());
                log.info("补偿支付记录：paymentNo={}, retryCount={}", logItem.getPaymentNo(), logItem.getRetryCount() + 1);

                if (logItem.getRetryCount() + 1 >= 3) {
                    logItem.setStatus(2);
                    logItem.setErrorMsg("支付超时，重试3次失败");
                    logItem.setUpdateTime(LocalDateTime.now());
                    paymentLogMapper.updateById(logItem);
                    log.info("支付记录标记为失败：paymentNo={}", logItem.getPaymentNo());
                }
            } catch (Exception e) {
                log.error("补偿支付记录失败：paymentNo={}", logItem.getPaymentNo(), e);
            }
        }

        log.info("支付补偿任务执行完成");
    }

    private String getStatusDesc(Integer status) {
        switch (status) {
            case 0:
                return "待支付";
            case 1:
                return "支付成功";
            case 2:
                return "支付失败";
            default:
                return "未知";
        }
    }
}