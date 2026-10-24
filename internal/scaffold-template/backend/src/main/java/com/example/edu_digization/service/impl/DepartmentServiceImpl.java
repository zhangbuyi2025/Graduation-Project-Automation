package com.example.edu_digization.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edu_digization.entity.Department;
import com.example.edu_digization.mapper.DepartmentMapper;
import com.example.edu_digization.service.IDepartmentService;
import org.springframework.stereotype.Service;

/**
 * 教研室 Service 实现
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {
}
