package com.example.edu_digization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.edu_digization.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 Mapper
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 按登录用户名或工号查询用户（支持用工号登录）
     */
    @Select("SELECT * FROM t_user WHERE username = #{keyword} OR user_no = #{keyword} LIMIT 1")
    User selectByUsername(@Param("keyword") String keyword);
}
