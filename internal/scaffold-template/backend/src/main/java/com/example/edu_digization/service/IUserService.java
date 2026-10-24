package com.example.edu_digization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edu_digization.entity.User;
import com.example.edu_digization.vo.UserInfoVO;

import java.util.List;

/**
 * 用户 Service
 */
public interface IUserService extends IService<User> {

    /**
     * 按登录用户名或工号查询用户
     */
    User getByLoginKey(String loginKey);

    /**
     * 查询教师/账号列表（支持按关键字、学院、教研室、状态筛选）
     */
    List<User> listTeachers(String keyword, Long collegeId, Long deptId, Integer status);

    /**
     * 转换为不含敏感字段的用户信息 VO
     */
    UserInfoVO toUserInfo(User user);
}
