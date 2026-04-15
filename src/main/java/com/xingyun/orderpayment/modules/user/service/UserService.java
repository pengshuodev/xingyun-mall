package com.xingyun.orderpayment.modules.user.service;

import com.xingyun.orderpayment.modules.user.dto.req.LoginReq;
import com.xingyun.orderpayment.modules.user.dto.req.RegisterReq;
import com.xingyun.orderpayment.modules.user.dto.resp.LoginResp;

public interface UserService {

    /**
     * 用户注册
     *
     * @param req 注册请求参数（用户名、密码、手机号）
     */
    void register(RegisterReq req);

    /**
     * 检查用户名是否已存在
     *
     * @param username 用户名
     * @return true-已存在，false-不存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 检查手机号是否已注册
     *
     * @param phone 手机号
     * @return true-已注册，false-未注册
     */
    boolean checkPhoneExists(String phone);

    /**
     * 用户登录
     *
     * @param req 登录请求参数（用户名、密码）
     * @return 登录响应参数（token、用户名、用户ID）
     */
    LoginResp login(LoginReq req);
}