package com.example.edu_digization.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录请求 DTO
 */
@Data
public class LoginDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户名 / 工号 */
    @NotBlank(message = "请输入用户名或工号")
    private String username;

    /** 密码 */
    @NotBlank(message = "请输入密码")
    private String password;
}
