package com.example.edu_digization.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edu_digization.entity.College;
import com.example.edu_digization.mapper.CollegeMapper;
import com.example.edu_digization.service.ICollegeService;
import org.springframework.stereotype.Service;

/**
 * 学院 Service 实现
 */
@Service
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements ICollegeService {
}
