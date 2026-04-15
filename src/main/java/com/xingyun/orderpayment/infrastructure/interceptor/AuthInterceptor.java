package com.xingyun.orderpayment.infrastructure.interceptor;

import com.xingyun.orderpayment.common.context.UserContext;
import com.xingyun.orderpayment.common.exception.BusinessException;
import com.xingyun.orderpayment.common.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行 OPTIONS 请求（跨域预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            throw new BusinessException(401, "未登录");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Long userId = jwtUtils.getUserIdFromToken(token);
        UserContext.setUserId(userId);

        log.debug("用户认证成功：userId={}", userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}