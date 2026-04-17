package com.example.watchaura.config;

import com.example.watchaura.interceptor.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        "classpath:/static/uploads/",
                        "file:uploads/",
                        "file:backend/uploads/"
                );
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                // 管理员页面路径 - 仅 Admin 和 Quản lý 可访问
                .addPathPatterns("/admin/**")
                // 收银页面路径 - Admin, Quản lý, Nhân viên 可访问
                .addPathPatterns("/ban-hang/**")
                // 用户专属页面 - 需要登录
                .addPathPatterns("/gio-hang")
                .addPathPatterns("/gio-hang/**")
                .addPathPatterns("/thanh-toan")
                .addPathPatterns("/thanh-toan/**")
                // /don-hang/** 和 /theo-doi-don-hang - KHÔNG yêu cầu đăng nhập (theo dõi đơn bằng mã)
                .addPathPatterns("/nguoidung/**")
                .addPathPatterns("/danh-gia/**")
                // 排除路径 - 不需要权限控制的路径
                .excludePathPatterns(
                        // 管理员登录页面（单独处理）
                        "/admin/dang-nhap",
                        "/admin/dang-nhap.html",
                        "/admin/login",
                        "/admin/login.html",
                        // 管理员公开页面
                        "/admin/403",
                        "/admin/403.html",
                        "/admin/error/**",
                        // 访客结账 - 不需要登录
                        "/checkout",
                        "/checkout/**",
                        // Theo dõi đơn hàng - không cần đăng nhập
                        "/don-hang",
                        "/don-hang/**",
                        "/theo-doi-don-hang",
                        "/theo-doi-don-hang/**"
                );
    }
}
