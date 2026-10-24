package com.example.edu_digization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edu_digization.entity.CourseInstance;
import com.example.edu_digization.vo.TeachingTaskVO;

import java.util.List;

/**
 * 课程实例 Service
 */
public interface ICourseInstanceService extends IService<CourseInstance> {

    /**
     * 某教师在某学期的教学任务
     */
    List<TeachingTaskVO> listTeachingTasks(Long teacherId, Long semesterId);
}
