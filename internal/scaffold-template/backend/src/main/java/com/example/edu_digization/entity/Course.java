package com.example.edu_digization.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程主数据（t_course）
 */
@Data
@TableName("t_course")
public class Course implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 课程 ID */
    @TableId(value = "course_id", type = IdType.AUTO)
    private Long courseId;

    /** 课程代码（唯一） */
    private String courseCode;

    /** 课程名称 */
    private String courseName;

    /** 学分 */
    private BigDecimal credit;

    /** 总学时 */
    private Integer hours;

    /** 课程类型：required 必修 / elective 选修 / general 通识 */
    private String courseType;

    /** 开课教研室 ID */
    private Long deptId;

    /** 状态：1 启用 / 0 停用 */
    private Integer status;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
