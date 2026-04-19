package com.xingyun.orderpayment.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.orderpayment.common.enums.ResultCodeEnum;
import com.xingyun.orderpayment.common.enums.UserStatusEnum;
import com.xingyun.orderpayment.common.exception.BusinessException;
import com.xingyun.orderpayment.common.utils.JwtUtils;
import com.xingyun.orderpayment.modules.user.dto.req.LoginReq;
import com.xingyun.orderpayment.modules.user.dto.req.RegisterReq;
import com.xingyun.orderpayment.modules.user.dto.resp.LoginResp;
import com.xingyun.orderpayment.modules.user.entity.User;
import com.xingyun.orderpayment.modules.user.mapper.UserMapper;
import com.xingyun.orderpayment.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public void register(RegisterReq req) {
        // 1. 验证用户名是否已存在
        if (checkUsernameExists(req.getUsername())) {
            throw new BusinessException(ResultCodeEnum.USERNAME_EXISTS);
        }
        // 2. 验证手机号是否已存在
        if (req.getPhone() != null && checkPhoneExists(req.getPhone())) {
            throw new BusinessException(ResultCodeEnum.PHONE_EXISTS);
        }
        // 3. 保存用户信息
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setPhone(req.getPhone());
        user.setStatus(UserStatusEnum.ENABLED.getCode());
        int inserted = userMapper.insert(user);
        if (inserted <= 0) {
            throw new BusinessException(ResultCodeEnum.INTERNAL_ERROR, "注册失败，请稍后重试");
        }
        log.info("用户注册成功：username={}, phone={}", req.getUsername(), req.getPhone());
    }

    @Override
    public boolean checkUsernameExists(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return userMapper.selectCount(wrapper) > 0;
    }

    @Override
    public LoginResp login(LoginReq req) {
        // 1. 根据用户名查询用户信息
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, req.getUsername());
        User user = userMapper.selectOne(wrapper);

        // 2.用户不存在
        if (user == null) {
            throw new BusinessException(ResultCodeEnum.PASSWORD_ERROR);
        }

        // 3. 密码匹配
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCodeEnum.PASSWORD_ERROR);
        }

        // 4. 检查用户状态（是否被禁用）
        UserStatusEnum status = UserStatusEnum.fromCodeOrDefault(
                user.getStatus(),
                UserStatusEnum.DISABLED
        );
        if (status.isDisabled()) {
            throw new BusinessException(ResultCodeEnum.USER_DISABLED);
        }

        // 5. 生成 token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        // 6. 更新最后登录时间和IP（IP可以从请求中获取，先留空）
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 7. 返回响应
        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUsername(user.getUsername());
        resp.setUserId(user.getId());

        return resp;
    }

}
