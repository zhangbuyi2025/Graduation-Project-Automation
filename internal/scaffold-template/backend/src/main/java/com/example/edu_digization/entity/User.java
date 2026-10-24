package com.example.edu_digization.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户（t_user，统一身份认证同步 + 本地扩展字段）
 * 密码使用 BCrypt 加密
 */
@Data
@TableName("t_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户 ID */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /** 工号 / 学工号（唯一） */
    private String userNo;

    /** 登录用户名 */
    private String username;

    /** BCrypt 加密密码；若走统一认证则可为空 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 角色：teacher / dean_chair / dean / admin / auditor */
    private String role;

    /** 所属学院 ID */
    private Long collegeId;

    /** 所属教研室 ID */
    private Long deptId;

    /** 邮箱 */
    private String email;

    /** 联系电话 */
    private String phone;

    /** 头像地址 */
    private String avatarUrl;

    /** 状态：1 启用 / 0 停用 */
    private Integer status;

    /** 最近登录时间 */
    private LocalDateTime lastLoginAt;

    /** 连续登录失败次数 */
    private Integer loginFailCnt;

    /** 密码到期时间 */
    private LocalDateTime pwdExpireAt;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
