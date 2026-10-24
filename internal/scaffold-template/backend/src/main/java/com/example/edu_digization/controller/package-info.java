/**
 * REST Controller 层
 * 统一前缀 /api，按角色分目录：
 *   /api/public/**    公开接口（登录、注册）—— SecurityConfig 中 permitAll
 *   /api/admin/**     教务处接口（教师管理、课程管理、中间审核）—— hasRole ADMIN
 *   /api/work/**      业务接口（档案提交、审核、归档查询）—— 登录即可
 * 返回体统一使用 common.Result 封装
 */
package com.example.edu_digization.controller;
