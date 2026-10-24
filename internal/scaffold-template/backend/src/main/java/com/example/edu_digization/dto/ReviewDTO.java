package com.example.edu_digization.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 审核请求 DTO（初审 / 中间审核 / 终审通用）
 */
@Data
public class ReviewDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 档案 ID */
    @NotNull(message = "档案 ID 不能为空")
    private Long archiveId;

    /** 审核结果：APPROVED 通过 / REJECTED 驳回 */
    private String result;

    /** 审核意见 / 驳回理由（驳回时必填） */
    private String comment;

    /** 被驳回的附件槽位，多个以逗号分隔，如 1,3,5；一键驳回可为空 */
    private String rejectedSlots;
}
