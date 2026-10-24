package com.example.edu_digization.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核记录 VO（含审核人、提交人、课程、学期等信息）
 */
@Data
public class ReviewLogVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long reviewId;
    private Long archiveId;

    /** 审核级别：1 初审 / 2 中间审核 / 3 终审 */
    private Integer reviewLevel;
    /** 审核人 ID / 姓名 / 工号 */
    private Long reviewerId;
    private String reviewerName;
    private String reviewerNo;

    /** 结果：APPROVED / REJECTED */
    private String result;
    /** 审核意见 */
    private String comment;
    /** 被驳回槽位 */
    private String rejectedSlots;
    /** 审核前/后状态 */
    private String reviewBeforeStatus;
    private String reviewAfterStatus;
    /** 审核时间 */
    private LocalDateTime reviewTime;

    /** 提交教师姓名 / 工号 */
    private String teacherName;
    private String teacherNo;
    /** 课程号 / 名称 / 班级 */
    private String courseCode;
    private String courseName;
    private String className;
    /** 学期名称 */
    private String semesterName;
    /** 教研室 / 学院名称 */
    private String deptName;
    private String collegeName;
}
