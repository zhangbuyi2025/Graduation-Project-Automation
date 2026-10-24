package com.example.edu_digization.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 课程 VO（课程主数据 + 教研室/学院名称，开课学院由关联自动推导）
 */
@Data
public class CourseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long courseId;
    /** 课程号 */
    private String courseCode;
    /** 课程名称 */
    private String courseName;
    /** 学分 */
    private BigDecimal credit;
    /** 学时 */
    private Integer hours;
    /** 课程类型：required / elective / general */
    private String courseType;
    /** 教研室 ID */
    private Long deptId;
    /** 教研室名称 */
    private String deptName;
    /** 学院 ID */
    private Long collegeId;
    /** 开课学院名称（由 t_department → t_college 推导） */
    private String collegeName;
    /** 状态：1 启用 / 0 停用 */
    private Integer status;
}
