package com.example.edu_digization.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edu_digization.entity.Course;
import com.example.edu_digization.mapper.CourseMapper;
import com.example.edu_digization.service.ICourseService;
import com.example.edu_digization.vo.CourseVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程 Service 实现
 */
@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements ICourseService {

    @Override
    public List<CourseVO> listCourses(String keyword, Long collegeId, Long deptId, String courseType) {
        return this.baseMapper.selectCourseVOList(keyword, collegeId, deptId, courseType);
    }
}
