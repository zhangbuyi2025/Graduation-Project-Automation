package com.example.edu_digization.utils;

import com.example.edu_digization.entity.User;
import com.example.edu_digization.exception.BusinessException;
import com.example.edu_digization.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 安全上下文工具：获取当前登录用户
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserMapper userMapper;

    /**
     * 获取当前登录用户名（JWT 中的 sub）
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException(401, "未登录或登录已过期");
        }
        return String.valueOf(authentication.getPrincipal());
    }

    /**
     * 获取当前登录用户完整信息（查库）
     */
    public User getCurrentUser() {
        String username = getCurrentUsername();
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(401, "用户不存在或已被删除");
        }
        return user;
    }
}
