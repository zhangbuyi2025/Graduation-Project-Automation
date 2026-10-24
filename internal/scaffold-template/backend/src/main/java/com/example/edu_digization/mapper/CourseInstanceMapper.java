package com.example.edu_digization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.edu_digization.entity.CourseInstance;
import com.example.edu_digization.vo.TeachingTaskVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程实例 Mapper
 */
public interface CourseInstanceMapper extends BaseMapper<CourseInstance> {

    /**
     * 查询某教师在某学期的教学任务（含课程/教研室/学院 + 课前/课后档案状态）
     */
    @Select("<script>"
            + "SELECT i.instance_id, i.course_id, i.semester_id, i.teacher_id, i.class_name, "
            + "       i.student_count, i.location, i.schedule, i.status AS instance_status, "
            + "       c.course_code, c.course_name, c.credit, c.hours, c.course_type, "
            + "       d.dept_id, d.dept_name, col.college_id, col.college_name, "
            + "       pre.archive_id  AS pre_archive_id,  pre.status  AS pre_status, "
            + "       post.archive_id AS post_archive_id, post.status AS post_status "
            + "FROM t_course_instance i "
            + "JOIN t_course c ON c.course_id = i.course_id "
            + "JOIN t_department d ON d.dept_id = c.dept_id "
            + "JOIN t_college col ON col.college_id = d.college_id "
            + "LEFT JOIN t_archive pre ON pre.instance_id = i.instance_id "
            + "     AND pre.archive_type = 'PRE' AND pre.is_deleted = 0 "
            + "LEFT JOIN t_archive post ON post.instance_id = i.instance_id "
            + "     AND post.archive_type = 'POST' AND post.is_deleted = 0 "
            + "WHERE i.status = 1 AND i.teacher_id = #{teacherId} "
            + "  <if test='semesterId != null'> AND i.semester_id = #{semesterId}</if>"
            + "ORDER BY i.instance_id"
            + "</script>")
    List<TeachingTaskVO> selectTeachingTasks(
            @Param("teacherId") Long teacherId,
            @Param("semesterId") Long semesterId);
}
