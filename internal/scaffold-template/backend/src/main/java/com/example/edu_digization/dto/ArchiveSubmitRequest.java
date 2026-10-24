package com.example.edu_digization.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 档案提交请求（内部对象，由 Controller 组装后交给 Service）
 */
@Data
public class ArchiveSubmitRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 课程实例 ID */
    private Long instanceId;
    /** 提交教师 ID */
    private Long teacherId;
    /** 所属教研室 ID */
    private Long deptId;
    /** 档案类型：PRE / POST */
    private String archiveType;
    /** 已存在档案 ID（重提时传入，新增时为 null） */
    private Long archiveId;
    /** 已落盘的附件列表 */
    private List<StoredFile> files;
}
