package com.xingyun.orderpayment.modules.order.task;

import com.xingyun.orderpayment.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderService orderService;

    /**
     * 每 2 分钟执行一次，扫描超时订单
     */
    @Scheduled(cron = "0 */2 * * * ?")
    public void closeTimeoutOrders() {
        log.info("========== 超时关单任务开始执行 ==========");
        long startTime = System.currentTimeMillis();

        try {
            orderService.closeTimeoutOrders();
        } catch (Exception e) {
            log.error("超时关单任务执行异常", e);
        }

        long endTime = System.currentTimeMillis();
        log.info("========== 超时关单任务执行结束，耗时：{} ms ==========", (endTime - startTime));
    }
}