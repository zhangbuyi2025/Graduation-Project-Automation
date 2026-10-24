package com.example.edu_digization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.edu_digization.entity.Archive;
import com.example.edu_digization.vo.ArchiveListVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 教学档案 Mapper
 */
public interface ArchiveMapper extends BaseMapper<Archive> {

    /**
     * 档案列表（含课程/学期/教研室/学院/教师信息），支持多条件筛选
     */
    @Select("<script>"
            + "SELECT a.archive_id, a.instance_id, a.teacher_id, a.dept_id, a.archive_type, a.status, "
            + "       a.version_no, a.submit_time, a.last_resubmit_time, "
            + "       c.course_code, c.course_name, i.class_name, s.semester_id, s.semester_name, "
            + "       d.dept_name, col.college_name, u.user_no AS teacher_no, u.real_name AS teacher_name "
            + "FROM t_archive a "
            + "JOIN t_course_instance i ON i.instance_id = a.instance_id "
            + "JOIN t_course c ON c.course_id = i.course_id "
            + "JOIN t_semester s ON s.semester_id = i.semester_id "
            + "JOIN t_user u ON u.user_id = a.teacher_id "
            + "JOIN t_department d ON d.dept_id = a.dept_id "
            + "JOIN t_college col ON col.college_id = d.college_id "
            + "<where> a.is_deleted = 0 "
            + "  <if test='teacherId != null'> AND a.teacher_id = #{teacherId}</if>"
            + "  <if test='collegeId != null'> AND d.college_id = #{collegeId}</if>"
            + "  <if test='deptId != null'> AND a.dept_id = #{deptId}</if>"
            + "  <if test='semesterId != null'> AND i.semester_id = #{semesterId}</if>"
            + "  <if test='archiveType != null and archiveType != \"\"'> AND a.archive_type = #{archiveType}</if>"
            + "  <if test='status != null and status != \"\"'> AND a.status = #{status}</if>"
            + "  <if test='keyword != null and keyword != \"\"'>"
            + "    AND (u.real_name LIKE CONCAT('%',#{keyword},'%') "
            + "         OR c.course_name LIKE CONCAT('%',#{keyword},'%'))"
            + "  </if>"
            + "</where>"
            + "ORDER BY a.submit_time DESC"
            + "</script>")
    List<ArchiveListVO> selectArchiveList(
            @Param("teacherId") Long teacherId,
            @Param("collegeId") Long collegeId,
            @Param("deptId") Long deptId,
            @Param("semesterId") Long semesterId,
            @Param("archiveType") String archiveType,
            @Param("status") String status,
            @Param("keyword") String keyword);

    /**
     * 按 ID 查询单条档案 VO
     */
    @Select("SELECT a.archive_id, a.instance_id, a.teacher_id, a.dept_id, a.archive_type, a.status, "
            + "       a.version_no, a.submit_time, a.last_resubmit_time, "
            + "       c.course_code, c.course_name, i.class_name, s.semester_id, s.semester_name, "
            + "       d.dept_name, col.college_name, u.user_no AS teacher_no, u.real_name AS teacher_name "
            + "FROM t_archive a "
            + "JOIN t_course_instance i ON i.instance_id = a.instance_id "
            + "JOIN t_course c ON c.course_id = i.course_id "
            + "JOIN t_semester s ON s.semester_id = i.semester_id "
            + "JOIN t_user u ON u.user_id = a.teacher_id "
            + "JOIN t_department d ON d.dept_id = a.dept_id "
            + "JOIN t_college col ON col.college_id = d.college_id "
            + "WHERE a.archive_id = #{archiveId} AND a.is_deleted = 0")
    ArchiveListVO selectArchiveVOById(@Param("archiveId") Long archiveId);
}
