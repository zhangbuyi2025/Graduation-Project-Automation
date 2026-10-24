package com.example.edu_digization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.edu_digization.entity.Course;
import com.example.edu_digization.vo.CourseVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 课程 Mapper
 */
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 课程列表（含教研室/学院名称，开课学院由关联自动推导），支持多条件筛选
     */
    @Select("<script>"
            + "SELECT c.course_id, c.course_code, c.course_name, c.credit, c.hours, c.course_type, "
            + "       c.dept_id, d.dept_name, d.college_id, col.college_name, c.status "
            + "FROM t_course c "
            + "JOIN t_department d ON d.dept_id = c.dept_id "
            + "JOIN t_college col ON col.college_id = d.college_id "
            + "<where>"
            + "  <if test='keyword != null and keyword != \"\"'>"
            + "    AND (c.course_code LIKE CONCAT('%',#{keyword},'%') "
            + "         OR c.course_name LIKE CONCAT('%',#{keyword},'%'))"
            + "  </if>"
            + "  <if test='collegeId != null'> AND d.college_id = #{collegeId}</if>"
            + "  <if test='deptId != null'> AND c.dept_id = #{deptId}</if>"
            + "  <if test='courseType != null and courseType != \"\"'> AND c.course_type = #{courseType}</if>"
            + "</where>"
            + "ORDER BY c.course_id"
            + "</script>")
    List<CourseVO> selectCourseVOList(
            @Param("keyword") String keyword,
            @Param("collegeId") Long collegeId,
            @Param("deptId") Long deptId,
            @Param("courseType") String courseType);
}
