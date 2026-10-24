package com.example.edu_digization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edu_digization.entity.AuditLog;

/**
 * 审计日志 Service
 */
public interface IAuditLogService extends IService<AuditLog> {

    /**
     * 记录一条审计日志（自动获取 IP / User-Agent）
     *
     * @param operatorId  操作人 ID（null 表示系统自动）
     * @param operatorRole 操作人角色
     * @param operation   操作类型
     * @param targetType  目标类型
     * @param targetId    目标 ID
     * @param detail      详情（JSON 字符串）
     * @param result      结果：SUCCESS / FAIL
     * @param errorMsg    失败信息（成功为 null）
     */
    void record(Long operatorId, String operatorRole, String operation,
                String targetType, Long targetId, String detail,
                String result, String errorMsg);
}
