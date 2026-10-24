package com.example.edu_digization.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 档案列表 VO（含课程/学期/教研室/学院冗余信息）
 */
@Data
public class ArchiveListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long archiveId;
    private Long instanceId;
    private Long teacherId;
    private Long deptId;

    /** 档案类型：PRE / POST */
    private String archiveType;
    /** 状态 */
    private String status;
    /** 版本号 */
    private Integer versionNo;
    /** 首次提交时间 */
    private LocalDateTime submitTime;
    /** 最后重提时间 */
    private LocalDateTime lastResubmitTime;

    /** 课程号 / 名称 */
    private String courseCode;
    private String courseName;
    /** 班级 */
    private String className;
    /** 学期 ID / 名称 */
    private Long semesterId;
    private String semesterName;
    /** 教研室名称 */
    private String deptName;
    /** 学院名称 */
    private String collegeName;

    /** 教师工号 / 姓名 */
    private String teacherNo;
    private String teacherName;
}
