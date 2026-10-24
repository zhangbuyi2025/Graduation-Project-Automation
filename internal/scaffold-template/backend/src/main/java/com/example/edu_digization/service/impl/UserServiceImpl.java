package com.example.edu_digization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edu_digization.entity.User;
import com.example.edu_digization.mapper.UserMapper;
import com.example.edu_digization.service.IUserService;
import com.example.edu_digization.vo.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 用户 Service 实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public User getByLoginKey(String loginKey) {
        return this.baseMapper.selectByUsername(loginKey);
    }

    @Override
    public List<User> listTeachers(String keyword, Long collegeId, Long deptId, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(User::getUserNo, keyword)
                    .or().like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword));
        }
        if (collegeId != null) {
            wrapper.eq(User::getCollegeId, collegeId);
        }
        if (deptId != null) {
            wrapper.eq(User::getDeptId, deptId);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByAsc(User::getUserId);
        return this.list(wrapper);
    }

    @Override
    public UserInfoVO toUserInfo(User user) {
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
