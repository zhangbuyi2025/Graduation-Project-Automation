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
 * 单个附件（t_attachment）
 * 课前固定 5 槽，课后可按需扩展；强制 PDF
 */
@Data
@TableName("t_attachment")
public class Attachment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 附件 ID */
    @TableId(value = "att_id", type = IdType.AUTO)
    private Long attId;

    /** 所属档案 ID */
    private Long archiveId;

    /** 槽位编号：课前 1–5，课后 1–N */
    private Integer slotIndex;

    /** 槽位名称，如 教学大纲、教案 */
    private String slotName;

    /** 原始文件名 */
    private String originalName;

    /** 存储文件名（UUID + 扩展名） */
    private String storageName;

    /** 存储路径（本地绝对路径或对象存储 Key） */
    private String storagePath;

    /** 对象存储桶（若用 MinIO/OSS） */
    private String storageBucket;

    /** 扩展名，校验用；强制 pdf */
    private String fileExt;

    /** 文件大小（字节） */
    private Long fileSize;

    /** SHA-256 哈希 */
    private String fileHash;

    /** MIME 类型 */
    private String mimeType;

    /** 是否在最近一次审核中被驳回：1 是 / 0 否 */
    private Integer isRejected;

    /** 最近一次被驳回的理由 */
    private String rejectReason;

    /** 随档案版本号递增 */
    private Integer uploadVersion;

    /** 上传人 ID */
    private Long uploaderId;

    /** 上传时间 */
    private LocalDateTime uploadTime;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
