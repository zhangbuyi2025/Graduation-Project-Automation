package com.example.edu_digization.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学期（t_semester）
 */
@Data
@TableName("t_semester")
public class Semester implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 学期 ID */
    @TableId(value = "semester_id", type = IdType.AUTO)
    private Long semesterId;

    /** 学期代码，如 2025-FALL、2026-SPRING */
    private String semesterCode;

    /** 学期名称，如 2025 秋季 */
    private String semesterName;

    /** 开始日期 */
    private LocalDate startDate;

    /** 结束日期 */
    private LocalDate endDate;

    /** 是否当前学期：1 是 / 0 否 */
    private Integer isCurrent;

    /** 档案提交截止日期 */
    private LocalDate archiveDeadline;

    /** 状态：1 正常 / 0 停用 */
    private Integer status;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
