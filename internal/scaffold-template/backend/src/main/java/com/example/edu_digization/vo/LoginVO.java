package com.example.edu_digization.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录返回 VO
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** JWT 令牌 */
    private String token;

    /** 当前登录用户信息 */
    private UserInfoVO userInfo;
}
