package com.example.edu_digization.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 教学任务 VO（教师视角：某学期所授课程，含课前/课后档案状态）
 */
@Data
public class TeachingTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 课程实例 ID */
    private Long instanceId;
    private Long courseId;
    private Long semesterId;
    private Long teacherId;

    /** 班级 */
    private String className;
    /** 选课人数 */
    private Integer studentCount;
    /** 上课地点 */
    private String location;
    /** 上课时间 */
    private String schedule;
    /** 实例状态：1 正常 / 0 停开 */
    private Integer instanceStatus;

    /** 课程号 */
    private String courseCode;
    /** 课程名称 */
    private String courseName;
    /** 学分 */
    private BigDecimal credit;
    /** 学时 */
    private Integer hours;
    /** 课程类型：required/elective/general */
    private String courseType;

    /** 教研室 ID / 名称 */
    private Long deptId;
    private String deptName;
    /** 学院 ID / 名称 */
    private Long collegeId;
    private String collegeName;

    /** 课前档案 ID（无则为 null） */
    private Long preArchiveId;
    /** 课前档案状态 */
    private String preStatus;
    /** 课后档案 ID */
    private Long postArchiveId;
    /** 课后档案状态 */
    private String postStatus;
}
