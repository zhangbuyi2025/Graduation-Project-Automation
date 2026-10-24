package com.example.edu_digization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审核记录（t_review_log）：每次初审/中间审核/终审/驳回都生成一条
 */
@Data
@TableName("t_review_log")
public class ReviewLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 审核记录 ID */
    @TableId(value = "review_id", type = IdType.AUTO)
    private Long reviewId;

    /** 档案 ID */
    private Long archiveId;

    /** 审核级别：1 初审（教研室主任）/ 2 中间审核（教务处）/ 3 终审（教学院长） */
    private Integer reviewLevel;

    /** 审核人 ID */
    private Long reviewerId;

    /** 审核时的角色快照，用于审计 */
    private String roleSnapshot;

    /** 结果：APPROVED 通过 / REJECTED 驳回 */
    private String result;

    /** 审核意见 / 驳回理由 */
    private String comment;

    /** 被驳回的附件槽位，多个以逗号分隔，如 1,3,5 */
    private String rejectedSlots;

    /** 审核前状态（快照） */
    private String reviewBeforeStatus;

    /** 审核后状态（快照） */
    private String reviewAfterStatus;

    /** 操作人 IP */
    private String operatorIp;

    /** 操作人 User-Agent */
    private String operatorUa;

    /** 审核时间 */
    private LocalDateTime reviewTime;
}
