package com.example.watchaura.interceptor;

import com.example.watchaura.annotation.RequiresRole;
import com.example.watchaura.entity.ChucVu;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.repository.KhachHangRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 权限拦截器
 * 在 Controller 执行前检查用户权限
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER_ID = "currentUserId";

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理 Controller 方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 获取方法上的 @RequiresRole 注解
        RequiresRole methodRole = handlerMethod.getMethodAnnotation(RequiresRole.class);

        // 获取类上的 @RequiresRole 注解
        RequiresRole classRole = handlerMethod.getBeanType().getAnnotation(RequiresRole.class);

        // 如果都没有定义注解，则放行
        if (methodRole == null && classRole == null) {
            return true;
        }

        // 合并类级别和方法级别的注解
        RequiresRole requiresRole = methodRole != null ? methodRole : classRole;

        // 检查是否需要登录
        if (requiresRole.requireAuth()) {
            // 检查用户是否登录
            HttpSession session = request.getSession();
            Integer userId = (Integer) session.getAttribute(SESSION_USER_ID);

            if (userId == null) {
                // 用户未登录，重定向到登录页面
                handleNotLoggedIn(request, response);
                return false;
            }

            // 获取用户信息（包含角色）
            KhachHang user = khachHangRepository.findByIdWithChucVu(userId).orElse(null);
            if (user == null) {
                handleNotLoggedIn(request, response);
                return false;
            }

            // 检查用户是否被禁用
            if (user.getTrangThai() != null && !user.getTrangThai()) {
                handleUserDisabled(request, response);
                return false;
            }

            // 检查角色权限
            if (!hasPermission(user, requiresRole)) {
                handleAccessDenied(request, response);
                return false;
            }
        }

        return true;
    }

    /**
     * 检查用户是否有权限
     */
    private boolean hasPermission(KhachHang user, RequiresRole requiresRole) {
        if (requiresRole.value() == null || requiresRole.value().length == 0) {
            return true; // 没有指定角色要求，只要登录即可
        }

        // 获取用户角色名称
        ChucVu chucVu = user.getChucVu();
        if (chucVu == null || chucVu.getTenChucVu() == null) {
            return false; // 用户没有角色，拒绝访问
        }

        String userRole = chucVu.getTenChucVu();

        // 将需要的角色转换为 Set（不区分大小写比较）
        Set<String> allowedRoles = new HashSet<>();
        for (String role : requiresRole.value()) {
            allowedRoles.add(role.toLowerCase().trim());
        }

        // 检查用户角色是否在允许列表中
        return allowedRoles.contains(userRole.toLowerCase().trim());
    }

    /**
     * 处理用户未登录的情况
     */
    private void handleNotLoggedIn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 检查是否是 AJAX 请求
        if (isAjaxRequest(request)) {
            sendJsonError(response, 401, "Vui lòng đăng nhập để tiếp tục!", "/dang-nhap");
        } else {
            // 保存原始请求路径，登录后可以重定向回来
            String originalUri = request.getRequestURI();
            String queryString = request.getQueryString();
            if (queryString != null) {
                originalUri += "?" + queryString;
            }
            request.getSession().setAttribute("originalUri", originalUri);
            response.sendRedirect("/dang-nhap?error=login_required");
        }
    }

    /**
     * 处理用户被禁用的情况
     */
    private void handleUserDisabled(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 清除 session
        request.getSession().invalidate();

        if (isAjaxRequest(request)) {
            sendJsonError(response, 403, "Tài khoản của bạn đã bị vô hiệu hóa!", "/dang-nhap");
        } else {
            response.sendRedirect("/dang-nhap?error=account_disabled");
        }
    }

    /**
     * 处理权限不足的情况
     */
    private void handleAccessDenied(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            sendJsonError(response, 403, "Bạn không có quyền truy cập trang này!", null);
        } else {
            // 保存原始请求路径
            String originalUri = request.getRequestURI();
            request.getSession().setAttribute("accessDeniedUri", originalUri);
            response.sendRedirect("/error/403");
        }
    }

    /**
     * 发送 JSON 错误响应
     */
    private void sendJsonError(HttpServletResponse response, int status, String message, String redirectUrl) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json;
        if (redirectUrl != null) {
            json = String.format("{\"error\": \"%s\", \"code\": %d, \"redirectUrl\": \"%s\"}", message, status, redirectUrl);
        } else {
            json = String.format("{\"error\": \"%s\", \"code\": %d}", message, status);
        }

        response.getWriter().write(json);
    }

    /**
     * 检查是否是 AJAX 请求
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith);
    }
}
