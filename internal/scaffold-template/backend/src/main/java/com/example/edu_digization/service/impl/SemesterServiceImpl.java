package com.example.edu_digization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edu_digization.entity.Semester;
import com.example.edu_digization.mapper.SemesterMapper;
import com.example.edu_digization.service.ISemesterService;
import org.springframework.stereotype.Service;

/**
 * 学期 Service 实现
 */
@Service
public class SemesterServiceImpl extends ServiceImpl<SemesterMapper, Semester> implements ISemesterService {

    @Override
    public Semester getCurrentSemester() {
        return this.getOne(new LambdaQueryWrapper<Semester>()
                .eq(Semester::getIsCurrent, 1)
                .eq(Semester::getStatus, 1)
                .last("LIMIT 1"));
    }
}
