package com.example.edu_digization.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息 VO（不含密码等敏感字段）
 */
@Data
public class UserInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String userNo;
    private String username;
    private String realName;
    /** 角色：teacher / dean_chair / dean / admin / auditor */
    private String role;
    private Long collegeId;
    private Long deptId;
    private String email;
    private String phone;
    private String avatarUrl;
    private Integer status;
}
