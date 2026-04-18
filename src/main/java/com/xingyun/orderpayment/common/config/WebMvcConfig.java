package com.xingyun.orderpayment.common.config;

import com.xingyun.orderpayment.infrastructure.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/api/payment/mock-pay",        // 放行模拟支付页面
                        "/api/payment/mock-pay/**",      // 放行模拟支付页面相关资源
                        "/api/payment/status",
                        "/api/payment/callback",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/doc.html"
                );
    }
}