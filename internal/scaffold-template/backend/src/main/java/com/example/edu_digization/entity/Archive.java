package com.example.edu_digization.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 教学档案（t_archive）：课前/课后一份对应一个课程实例
 *
 * 状态流转（三级审核）：
 *   PENDING 待初审 → FIRST_REVIEW 待中间审核 → MIDDLE_REVIEW 待终审
 *   → ARCHIVED 已归档；任意环节驳回 → REJECTED 已驳回，教师可重提
 */
@Data
@TableName("t_archive")
public class Archive implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 档案 ID */
    @TableId(value = "archive_id", type = IdType.AUTO)
    private Long archiveId;

    /** 课程实例 ID */
    private Long instanceId;

    /** 提交教师（冗余，便于查询 + 约束校验） */
    private Long teacherId;

    /** 所属教研室（提交时选择） */
    private Long deptId;

    /** 档案类型：PRE 课前 / POST 课后 */
    private String archiveType;

    /** 状态：NOT_SUBMITTED / PENDING / FIRST_REVIEW / MIDDLE_REVIEW / SECOND_REVIEW / ARCHIVED / REJECTED */
    private String status;

    /** 初审人（教研室主任） */
    private Long firstReviewerId;

    /** 初审时间 */
    private LocalDateTime firstReviewAt;

    /** 中间审核人（教务处） */
    private Long middleReviewerId;

    /** 中间审核时间 */
    private LocalDateTime middleReviewAt;

    /** 终审人（教学院长） */
    private Long secondReviewerId;

    /** 终审时间 */
    private LocalDateTime secondReviewAt;

    /** 首次提交时间 */
    private LocalDateTime submitTime;

    /** 最后一次重提时间 */
    private LocalDateTime lastResubmitTime;

    /** 版本号（每次重提 +1） */
    private Integer versionNo;

    /** 逻辑删除：0 正常 / 1 已删除 */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
