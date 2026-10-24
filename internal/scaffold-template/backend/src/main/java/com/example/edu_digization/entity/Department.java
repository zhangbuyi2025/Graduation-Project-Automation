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
 * 教研室（字典表 t_department）
 */
@Data
@TableName("t_department")
public class Department implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 教研室 ID */
    @TableId(value = "dept_id", type = IdType.AUTO)
    private Long deptId;

    /** 教研室代码（唯一） */
    private String deptCode;

    /** 教研室名称 */
    private String deptName;

    /** 所属学院 ID */
    private Long collegeId;

    /** 教研室主任用户 ID（外键 t_user） */
    private Long chairId;

    /** 状态：1 启用 / 0 停用 */
    private Integer status;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
