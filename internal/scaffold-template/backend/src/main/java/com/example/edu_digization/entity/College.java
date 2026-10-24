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
 * 学院（字典表 t_college）
 */
@Data
@TableName("t_college")
public class College implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 学院 ID */
    @TableId(value = "college_id", type = IdType.AUTO)
    private Long collegeId;

    /** 学院代码（唯一，如 CS、EE） */
    private String collegeCode;

    /** 学院名称 */
    private String collegeName;

    /** 状态：1 启用 / 0 停用 */
    private Integer status;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
