package com.example.edu_digization.controller;

import com.example.edu_digization.common.Result;
import com.example.edu_digization.dto.LoginDTO;
import com.example.edu_digization.entity.User;
import com.example.edu_digization.exception.BusinessException;
import com.example.edu_digization.service.IAuditLogService;
import com.example.edu_digization.service.IUserService;
import com.example.edu_digization.utils.JwtTokenProvider;
import com.example.edu_digization.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 认证控制器（公开接口）
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class AuthController {

    private final IUserService userService;
    private final IAuditLogService auditLogService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        String loginKey = dto.getUsername().trim();

        User user = userService.getByLoginKey(loginKey);
        if (user == null) {
            auditLogService.record(null, null, "LOGIN", "USER", null,
                    "{\"loginKey\":\"" + loginKey + "\"}", "FAIL", "用户不存在");
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已停用，请联系教务处");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BusinessException("该账号走统一身份认证，请通过统一身份认证入口登录");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            int failCnt = (user.getLoginFailCnt() == null ? 0 : user.getLoginFailCnt()) + 1;
            user.setLoginFailCnt(failCnt);
            userService.updateById(user);
            auditLogService.record(user.getUserId(), user.getRole(), "LOGIN", "USER",
                    user.getUserId(), null, "FAIL", "密码错误");
            throw new BusinessException("用户名或密码错误");
        }

        // 登录成功：重置失败计数、记录登录时间
        user.setLoginFailCnt(0);
        user.setLastLoginAt(LocalDateTime.now());
        userService.updateById(user);

        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserInfo(userService.toUserInfo(user));

        auditLogService.record(user.getUserId(), user.getRole(), "LOGIN", "USER",
                user.getUserId(), null, "SUCCESS", null);

        return Result.success(vo);
    }
}
