package com.example.edu_digization.common;

/**
 * 档案状态常量
 *
 * 三级审核状态流转：
 *   PENDING 待初审
 *   → FIRST_REVIEW 待中间审核
 *   → MIDDLE_REVIEW 待终审
 *   → ARCHIVED 已归档
 * 任意环节驳回 → REJECTED 已驳回
 */
public final class ArchiveStatus {

    private ArchiveStatus() {
    }

    /** 未提交 */
    public static final String NOT_SUBMITTED = "NOT_SUBMITTED";
    /** 待初审（教研室主任） */
    public static final String PENDING = "PENDING";
    /** 待中间审核（教务处，已过初审） */
    public static final String FIRST_REVIEW = "FIRST_REVIEW";
    /** 待终审（教学院长，已过中间审核） */
    public static final String MIDDLE_REVIEW = "MIDDLE_REVIEW";
    /** 终审中 */
    public static final String SECOND_REVIEW = "SECOND_REVIEW";
    /** 已归档 */
    public static final String ARCHIVED = "ARCHIVED";
    /** 已驳回 */
    public static final String REJECTED = "REJECTED";
}
