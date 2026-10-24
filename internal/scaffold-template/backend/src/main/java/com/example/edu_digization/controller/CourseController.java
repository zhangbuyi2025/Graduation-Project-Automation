package com.example.edu_digization.controller;

import com.example.edu_digization.common.Result;
import com.example.edu_digization.dto.CourseDTO;
import com.example.edu_digization.entity.Course;
import com.example.edu_digization.exception.BusinessException;
import com.example.edu_digization.service.IAuditLogService;
import com.example.edu_digization.service.ICourseService;
import com.example.edu_digization.vo.CourseVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 课程主数据管理控制器（教务处）
 */
@RestController
@RequestMapping("/api/admin/course")
@RequiredArgsConstructor
public class CourseController {

    private final ICourseService courseService;
    private final IAuditLogService auditLogService;

    /**
     * 课程列表（支持关键字、学院、教研室、类型筛选）
     */
    @GetMapping
    public Result<List<CourseVO>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "collegeId", required = false) Long collegeId,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "courseType", required = false) String courseType) {
        return Result.success(courseService.listCourses(keyword, collegeId, deptId, courseType));
    }

    /**
     * 新增课程
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CourseDTO dto) {
        if (dto.getCourseCode() == null || dto.getCourseCode().isBlank()) {
            throw new BusinessException("课程号不能为空");
        }
        if (courseService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                .eq(Course::getCourseCode, dto.getCourseCode())) > 0) {
            throw new BusinessException("课程号已存在：" + dto.getCourseCode());
        }

        Course course = new Course();
        BeanUtils.copyProperties(dto, course);
        if (course.getCourseType() == null) {
            course.setCourseType("required");
        }
        course.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        courseService.save(course);

        auditLogService.record(null, "admin", "CREATE_COURSE", "COURSE",
                course.getCourseId(), null, "SUCCESS", null);
        return Result.success();
    }

    /**
     * 编辑课程
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @Valid @RequestBody CourseDTO dto) {
        Course course = courseService.getById(id);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        if (dto.getCourseCode() != null && !dto.getCourseCode().isBlank()
                && !dto.getCourseCode().equals(course.getCourseCode())) {
            if (courseService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Course>()
                    .eq(Course::getCourseCode, dto.getCourseCode())) > 0) {
                throw new BusinessException("课程号已存在：" + dto.getCourseCode());
            }
            course.setCourseCode(dto.getCourseCode());
        }

        course.setCourseName(dto.getCourseName());
        course.setCredit(dto.getCredit());
        course.setHours(dto.getHours());
        if (dto.getCourseType() != null) {
            course.setCourseType(dto.getCourseType());
        }
        course.setDeptId(dto.getDeptId());
        if (dto.getStatus() != null) {
            course.setStatus(dto.getStatus());
        }
        courseService.updateById(course);

        auditLogService.record(null, "admin", "UPDATE_COURSE", "COURSE", id, null, "SUCCESS", null);
        return Result.success();
    }

    /**
     * 删除（停用，软删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        Course course = courseService.getById(id);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }
        course.setStatus(0);
        courseService.updateById(course);

        auditLogService.record(null, "admin", "DELETE_COURSE", "COURSE", id, null, "SUCCESS", null);
        return Result.success();
    }
}
