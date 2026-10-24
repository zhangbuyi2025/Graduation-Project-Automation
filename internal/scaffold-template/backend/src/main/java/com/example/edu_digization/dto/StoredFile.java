package com.example.edu_digization.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 已落盘文件的元数据（文件存储服务返回）
 */
@Data
public class StoredFile implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 槽位编号 */
    private Integer slotIndex;
    /** 槽位名称 */
    private String slotName;
    /** 原始文件名 */
    private String originalName;
    /** 存储文件名（UUID + 扩展名） */
    private String storageName;
    /** 存储路径 */
    private String storagePath;
    /** 扩展名 */
    private String fileExt;
    /** 文件大小（字节） */
    private Long fileSize;
    /** SHA-256 哈希 */
    private String fileHash;
    /** MIME 类型 */
    private String mimeType;
}
