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
 * 通用操作审计日志（t_audit_log）：覆盖所有关键写操作
 */
@Data
@TableName("t_audit_log")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志 ID */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    /** 操作人 ID（为空表示系统自动） */
    private Long operatorId;

    /** 操作人角色快照 */
    private String operatorRole;

    /** 操作类型：LOGIN / SUBMIT / RESUBMIT / FIRST_REVIEW / MIDDLE_REVIEW / SECOND_REVIEW / EXPORT / DELETE 等 */
    private String operation;

    /** 目标类型：ARCHIVE / ATTACHMENT / USER / COURSE 等 */
    private String targetType;

    /** 目标 ID */
    private Long targetId;

    /** 详细内容（JSON：前后快照 / 业务字段） */
    private String detail;

    /** 操作人 IP */
    private String operatorIp;

    /** 操作人 User-Agent */
    private String operatorUa;

    /** 操作结果：SUCCESS / FAIL */
    private String result;

    /** 失败时的错误信息 */
    private String errorMsg;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
