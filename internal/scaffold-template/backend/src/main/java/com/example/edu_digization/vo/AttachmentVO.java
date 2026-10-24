package com.example.edu_digization.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 附件 VO
 */
@Data
public class AttachmentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long attId;
    private Long archiveId;
    /** 槽位编号 */
    private Integer slotIndex;
    /** 槽位名称 */
    private String slotName;
    /** 原始文件名 */
    private String originalName;
    /** 存储文件名 */
    private String storageName;
    /** 扩展名 */
    private String fileExt;
    /** 文件大小（字节） */
    private Long fileSize;
    /** MIME 类型 */
    private String mimeType;
    /** 是否被驳回 */
    private Integer isRejected;
    /** 驳回理由 */
    private String rejectReason;
    /** 上传版本 */
    private Integer uploadVersion;
    /** 上传人 ID */
    private Long uploaderId;
    /** 上传时间 */
    private LocalDateTime uploadTime;
}
