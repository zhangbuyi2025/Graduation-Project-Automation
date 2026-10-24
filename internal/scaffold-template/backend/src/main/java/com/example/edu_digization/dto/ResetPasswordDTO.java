package com.example.edu_digization.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 重置密码 DTO
 */
@Data
public class ResetPasswordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 新密码（明文，后端 BCrypt 加密） */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
