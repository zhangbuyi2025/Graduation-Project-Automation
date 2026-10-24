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
 * 课程实例（t_course_instance）：某学期某教师开的某门课（教学任务）
 */
@Data
@TableName("t_course_instance")
public class CourseInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 课程实例 ID */
    @TableId(value = "instance_id", type = IdType.AUTO)
    private Long instanceId;

    /** 课程 ID */
    private Long courseId;

    /** 学期 ID */
    private Long semesterId;

    /** 授课教师 ID */
    private Long teacherId;

    /** 班级，如 2023 计算机 1 班 */
    private String className;

    /** 选课人数 */
    private Integer studentCount;

    /** 上课地点 */
    private String location;

    /** 上课时间，如 周一 1-2 节 */
    private String schedule;

    /** 状态：1 正常 / 0 停开 */
    private Integer status;

    /** 创建时间 */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
