package com.example.edu_digization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edu_digization.entity.Semester;

/**
 * 学期 Service
 */
public interface ISemesterService extends IService<Semester> {

    /**
     * 获取当前学期（is_current=1）
     */
    Semester getCurrentSemester();
}
