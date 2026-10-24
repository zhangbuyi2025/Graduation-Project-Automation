package com.example.edu_digization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edu_digization.entity.Course;
import com.example.edu_digization.vo.CourseVO;

import java.util.List;

/**
 * 课程 Service
 */
public interface ICourseService extends IService<Course> {

    /**
     * 课程列表（含开课学院/教研室），支持多条件筛选
     */
    List<CourseVO> listCourses(String keyword, Long collegeId, Long deptId, String courseType);
}
