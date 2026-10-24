package com.example.edu_digization.vo;

import com.example.edu_digization.entity.Attachment;
import com.example.edu_digization.entity.ReviewLog;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 档案详情 VO（档案信息 + 当前版本附件 + 全部审核记录）
 */
@Data
public class ArchiveDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 档案基本信息 */
    private ArchiveListVO archive;

    /** 当前版本附件列表 */
    private List<Attachment> attachments;

    /** 全部审核记录（按时间正序） */
    private List<ReviewLog> reviewLogs;
}
