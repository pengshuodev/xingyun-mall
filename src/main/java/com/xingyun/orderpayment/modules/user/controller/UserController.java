package com.xingyun.orderpayment.modules.user.controller;

import com.xingyun.orderpayment.common.Result;
import com.xingyun.orderpayment.modules.user.dto.req.LoginReq;
import com.xingyun.orderpayment.modules.user.dto.req.RegisterReq;
import com.xingyun.orderpayment.modules.user.dto.resp.LoginResp;
import com.xingyun.orderpayment.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "用户模块")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@RequestBody @Valid RegisterReq req) {
        log.info("用户注册请求：username={}, phone={}", req.getUsername(), req.getPhone());
        userService.register(req);
        return Result.success();
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResp> login(@RequestBody @Valid LoginReq req) {
        log.info("用户登录请求：username={}", req.getUsername());
        LoginResp resp = userService.login(req);
        return Result.success(resp);
    }
}
