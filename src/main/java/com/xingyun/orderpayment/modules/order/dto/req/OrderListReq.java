package com.xingyun.orderpayment.modules.order.dto.req;

import lombok.Data;

@Data
public class OrderListReq {

    private Integer pageNum = 1;  // 页码，默认1

    private Integer pageSize = 10;  // 每页条数，默认10

    private Integer status;  // 状态筛选（可选），0-待支付 1-已支付 2-已取消 3-已关闭
}