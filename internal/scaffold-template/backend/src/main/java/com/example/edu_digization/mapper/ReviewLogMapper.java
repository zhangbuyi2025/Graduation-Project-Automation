package com.example.edu_digization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.edu_digization.entity.ReviewLog;
import com.example.edu_digization.vo.ReviewLogVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 审核记录 Mapper
 */
public interface ReviewLogMapper extends BaseMapper<ReviewLog> {

    /**
     * 审核记录列表（含审核人/提交人/课程/学期/教研室/学院），支持多条件筛选
     */
    @Select("<script>"
            + "SELECT r.review_id, r.archive_id, r.review_level, r.reviewer_id, "
            + "       u.real_name AS reviewer_name, u.user_no AS reviewer_no, "
            + "       r.result, r.comment, r.rejected_slots, "
            + "       r.review_before_status, r.review_after_status, r.review_time, "
            + "       t.real_name AS teacher_name, t.user_no AS teacher_no, "
            + "       c.course_code, c.course_name, i.class_name, s.semester_name, "
            + "       d.dept_name, col.college_name "
            + "FROM t_review_log r "
            + "JOIN t_user u ON u.user_id = r.reviewer_id "
            + "JOIN t_archive a ON a.archive_id = r.archive_id "
            + "JOIN t_user t ON t.user_id = a.teacher_id "
            + "JOIN t_course_instance i ON i.instance_id = a.instance_id "
            + "JOIN t_course c ON c.course_id = i.course_id "
            + "JOIN t_semester s ON s.semester_id = i.semester_id "
            + "JOIN t_department d ON d.dept_id = a.dept_id "
            + "JOIN t_college col ON col.college_id = d.college_id "
            + "<where>"
            + "  <if test='collegeId != null'> AND d.college_id = #{collegeId}</if>"
            + "  <if test='deptId != null'> AND a.dept_id = #{deptId}</if>"
            + "  <if test='semesterId != null'> AND i.semester_id = #{semesterId}</if>"
            + "  <if test='reviewLevel != null'> AND r.review_level = #{reviewLevel}</if>"
            + "  <if test='result != null and result != \"\"'> AND r.result = #{result}</if>"
            + "  <if test='keyword != null and keyword != \"\"'>"
            + "    AND (t.real_name LIKE CONCAT('%',#{keyword},'%') "
            + "         OR u.real_name LIKE CONCAT('%',#{keyword},'%'))"
            + "  </if>"
            + "</where>"
            + "ORDER BY r.review_time DESC"
            + "</script>")
    List<ReviewLogVO> selectReviewLogList(
            @Param("collegeId") Long collegeId,
            @Param("deptId") Long deptId,
            @Param("semesterId") Long semesterId,
            @Param("reviewLevel") Integer reviewLevel,
            @Param("result") String result,
            @Param("keyword") String keyword);
}
