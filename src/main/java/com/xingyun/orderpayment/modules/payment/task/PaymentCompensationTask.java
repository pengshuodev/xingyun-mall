package com.xingyun.orderpayment.modules.payment.task;

import com.xingyun.orderpayment.modules.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PaymentCompensationTask {

    private final PaymentService paymentService;

    /**
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void compensate() {
        log.info("定时补偿任务开始执行");
        try {
            paymentService.compensatePendingPayments();
        } catch (Exception e) {
            log.error("补偿任务执行失败", e);
        }
        log.info("定时补偿任务执行结束");
    }
}