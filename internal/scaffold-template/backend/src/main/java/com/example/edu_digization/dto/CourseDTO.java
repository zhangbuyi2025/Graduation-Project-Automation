package com.example.edu_digization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 课程新增、编辑 DTO
 */
@Data
public class CourseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 课程号（新增时必填） */
    private String courseCode;

    /** 课程名称 */
    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    /** 学分 */
    private BigDecimal credit;

    /** 学时 */
    private Integer hours;

    /** 课程类型：required / elective / general */
    private String courseType;

    /** 开课教研室 ID */
    @NotNull(message = "所属教研室不能为空")
    private Long deptId;

    /** 状态：1 启用 / 0 停用 */
    private Integer status;
}
