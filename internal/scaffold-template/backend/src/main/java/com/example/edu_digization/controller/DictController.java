package com.example.edu_digization.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.edu_digization.common.Result;
import com.example.edu_digization.entity.College;
import com.example.edu_digization.entity.Department;
import com.example.edu_digization.entity.Semester;
import com.example.edu_digization.service.ICollegeService;
import com.example.edu_digization.service.IDepartmentService;
import com.example.edu_digization.service.ISemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 基础字典控制器：为各端下拉框提供学院 / 教研室 / 学期数据
 */
@RestController
@RequestMapping("/api/work")
@RequiredArgsConstructor
public class DictController {

    private final ICollegeService collegeService;
    private final IDepartmentService departmentService;
    private final ISemesterService semesterService;

    /** 学院列表（默认只返回启用的） */
    @GetMapping("/college")
    public Result<List<College>> colleges(
            @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(College::getStatus, status);
        }
        wrapper.orderByAsc(College::getCollegeId);
        return Result.success(collegeService.list(wrapper));
    }

    /** 教研室列表（可按学院过滤） */
    @GetMapping("/department")
    public Result<List<Department>> departments(
            @RequestParam(value = "collegeId", required = false) Long collegeId,
            @RequestParam(value = "status", required = false) Integer status) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        if (collegeId != null) {
            wrapper.eq(Department::getCollegeId, collegeId);
        }
        if (status != null) {
            wrapper.eq(Department::getStatus, status);
        }
        wrapper.orderByAsc(Department::getDeptId);
        return Result.success(departmentService.list(wrapper));
    }

    /** 学期列表 */
    @GetMapping("/semester")
    public Result<List<Semester>> semesters() {
        LambdaQueryWrapper<Semester> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Semester::getSemesterId);
        return Result.success(semesterService.list(wrapper));
    }
}
