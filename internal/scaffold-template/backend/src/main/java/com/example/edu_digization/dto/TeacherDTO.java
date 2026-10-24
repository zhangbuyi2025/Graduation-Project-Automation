package com.example.edu_digization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 教师/账号新增、编辑 DTO
 */
@Data
public class TeacherDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 工号 */
    @NotBlank(message = "工号不能为空")
    private String userNo;

    /** 登录用户名（新增时必填） */
    private String username;

    /** 初始密码（新增时必填，编辑时留空表示不修改） */
    private String password;

    /** 真实姓名 */
    @NotBlank(message = "姓名不能为空")
    private String realName;

    /** 角色：teacher / dean_chair / dean / auditor */
    @NotBlank(message = "角色不能为空")
    private String role;

    /** 所属学院 ID */
    private Long collegeId;

    /** 所属教研室 ID */
    private Long deptId;

    /** 邮箱 */
    private String email;

    /** 联系电话 */
    private String phone;

    /** 状态：1 启用 / 0 停用 */
    private Integer status;
}
