package com.xingyun.orderpayment.modules.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingyun.orderpayment.modules.payment.entity.PaymentLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface PaymentLogMapper extends BaseMapper<PaymentLog> {

    /**
     * 查询待补偿的支付记录（待支付且超过10分钟）
     */
    @Select("SELECT * FROM t_payment_log WHERE status = 0 AND create_time < DATE_SUB(NOW(), INTERVAL 10 MINUTE) AND retry_count < 3")
    List<PaymentLog> selectPendingCompensation();

    /**
     * 增加重试次数
     */
    @Update("UPDATE t_payment_log SET retry_count = retry_count + 1 WHERE payment_no = #{paymentNo}")
    void incrementRetryCount(String paymentNo);
}