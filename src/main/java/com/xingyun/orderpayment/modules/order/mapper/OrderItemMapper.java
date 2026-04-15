package com.xingyun.orderpayment.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingyun.orderpayment.modules.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 批量插入订单明细
     */
    int insertBatch(@Param("list") List<OrderItem> orderItems);
}