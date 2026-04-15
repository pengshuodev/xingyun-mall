package com.xingyun.orderpayment.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingyun.orderpayment.modules.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}