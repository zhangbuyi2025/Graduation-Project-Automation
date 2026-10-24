package com.example.edu_digization.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类
 * 用于生成 Spring Security 兼容的 BCrypt 加密密码
 */
public class PasswordEncoderUtil {

    // 直接用 Spring Security 官方的加密器
    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    /**
     * 明文密码 → 加密成 BCrypt 字符串
     * @param rawPassword 明文密码，例如：123456
     * @return 加密后的密码（存到数据库里）
     */
    public static String encode(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    // 测试：运行 main 方法就能生成加密密码
    public static void main(String[] args) {
        // 你要加密的明文密码（种子账号统一初始密码）
        String rawPassword = "123456";

        // 生成加密密码
        String encodedPassword = encode(rawPassword);

        // 控制台输出结果
        System.out.println("明文密码：" + rawPassword);
        System.out.println("BCrypt 加密密码：" + encodedPassword);
    }
}