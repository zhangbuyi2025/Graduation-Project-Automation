package com.example.edu_digization.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edu_digization.entity.CourseInstance;
import com.example.edu_digization.mapper.CourseInstanceMapper;
import com.example.edu_digization.service.ICourseInstanceService;
import com.example.edu_digization.vo.TeachingTaskVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程实例 Service 实现
 */
@Service
public class CourseInstanceServiceImpl extends ServiceImpl<CourseInstanceMapper, CourseInstance>
        implements ICourseInstanceService {

    @Override
    public List<TeachingTaskVO> listTeachingTasks(Long teacherId, Long semesterId) {
        return this.baseMapper.selectTeachingTasks(teacherId, semesterId);
    }
}
