package com.xingyun.orderpayment.modules.user.dto.resp;

import lombok.Data;

@Data
public class LoginResp {

    private String token;

    private String username;

    private Long userId;
}