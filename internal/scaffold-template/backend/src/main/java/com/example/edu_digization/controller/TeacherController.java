package com.example.edu_digization.controller;

import com.example.edu_digization.common.Result;
import com.example.edu_digization.dto.ResetPasswordDTO;
import com.example.edu_digization.dto.TeacherDTO;
import com.example.edu_digization.entity.User;
import com.example.edu_digization.exception.BusinessException;
import com.example.edu_digization.service.IAuditLogService;
import com.example.edu_digization.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 教师 / 账号管理控制器（教务处）
 */
@RestController
@RequestMapping("/api/admin/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final IUserService userService;
    private final IAuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 教师账号列表（支持关键字、学院、教研室、状态筛选）
     */
    @GetMapping
    public Result<List<User>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "collegeId", required = false) Long collegeId,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(userService.listTeachers(keyword, collegeId, deptId, status));
    }

    /**
     * 新增教师账号
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody TeacherDTO dto) {
        // 工号唯一性
        if (userService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUserNo, dto.getUserNo())) > 0) {
            throw new BusinessException("工号已存在：" + dto.getUserNo());
        }

        String username = (dto.getUsername() == null || dto.getUsername().isBlank())
                ? dto.getUserNo() : dto.getUsername();
        // 用户名唯一性
        if (userService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)) > 0) {
            throw new BusinessException("登录用户名已存在：" + username);
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new BusinessException("初始密码不能为空");
        }

        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        user.setLoginFailCnt(0);
        userService.save(user);

        auditLogService.record(null, "admin", "CREATE_USER", "USER",
                user.getUserId(), null, "SUCCESS", null);
        return Result.success();
    }

    /**
     * 编辑教师信息
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id, @Valid @RequestBody TeacherDTO dto) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 工号若被修改，校验唯一性
        if (!user.getUserNo().equals(dto.getUserNo())) {
            if (userService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                    .eq(User::getUserNo, dto.getUserNo())) > 0) {
                throw new BusinessException("工号已存在：" + dto.getUserNo());
            }
        }

        user.setUserNo(dto.getUserNo());
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole());
        user.setCollegeId(dto.getCollegeId());
        user.setDeptId(dto.getDeptId());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }
        // 编辑表单若携带密码则一并更新
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        userService.updateById(user);

        auditLogService.record(null, "admin", "UPDATE_USER", "USER", id, null, "SUCCESS", null);
        return Result.success();
    }

    /**
     * 启用 / 停用账号
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id,
                                     @RequestParam("status") Integer status) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status == 1 ? 1 : 0);
        userService.updateById(user);

        auditLogService.record(null, "admin", "UPDATE_USER_STATUS", "USER", id,
                "{\"status\":" + user.getStatus() + "}", "SUCCESS", null);
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable("id") Long id,
                                      @Valid @RequestBody ResetPasswordDTO dto) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setLoginFailCnt(0);
        userService.updateById(user);

        auditLogService.record(null, "admin", "RESET_PASSWORD", "USER", id, null, "SUCCESS", null);
        return Result.success();
    }

    /**
     * 删除（停用，软删除）：将状态置为停用
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(0);
        userService.updateById(user);

        auditLogService.record(null, "admin", "DELETE_USER", "USER", id, null, "SUCCESS", null);
        return Result.success();
    }
}
