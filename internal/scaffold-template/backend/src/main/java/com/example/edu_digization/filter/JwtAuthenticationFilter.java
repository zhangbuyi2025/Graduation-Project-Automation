package com.example.edu_digization.filter;

import com.example.edu_digization.utils.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // ============ 白名单：放行公开接口，跳过JWT校验 ============
        String requestUri = request.getRequestURI();
        if (requestUri.startsWith("/api/public/")
                || requestUri.startsWith("/api/forget/")) {
            filterChain.doFilter(request, response);
            return;
        }
        // =========================================================

        final String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        String username;
        String role;

        try {
            username = jwtTokenProvider.getUsernameByToken(token);
            role = jwtTokenProvider.getRoleByToken(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (!jwtTokenProvider.isTokenExpired(token)) {
                // 数据库角色统一转大写后加 ROLE_ 前缀：
                // teacher→ROLE_TEACHER / dean_chair→ROLE_DEAN_CHAIR /
                // dean→ROLE_DEAN / admin→ROLE_ADMIN / auditor→ROLE_AUDITOR
                String authorityRole = role == null ? "" : role.toUpperCase();
                List<org.springframework.security.core.GrantedAuthority> auths
                        = AuthorityUtils.createAuthorityList("ROLE_" + authorityRole);

                UsernamePasswordAuthenticationToken authToken
                        = new UsernamePasswordAuthenticationToken(username, null, auths);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}