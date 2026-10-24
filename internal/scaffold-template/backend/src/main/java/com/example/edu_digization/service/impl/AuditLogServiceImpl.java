package com.example.edu_digization.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edu_digization.entity.AuditLog;
import com.example.edu_digization.mapper.AuditLogMapper;
import com.example.edu_digization.service.IAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 审计日志 Service 实现
 */
@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog> implements IAuditLogService {

    @Override
    public void record(Long operatorId, String operatorRole, String operation,
                       String targetType, Long targetId, String detail,
                       String result, String errorMsg) {
        try {
            AuditLog log = new AuditLog();
            log.setOperatorId(operatorId);
            log.setOperatorRole(operatorRole);
            log.setOperation(operation);
            log.setTargetType(targetType);
            log.setTargetId(targetId);
            log.setDetail(detail);
            log.setResult(result == null ? "SUCCESS" : result);
            log.setErrorMsg(errorMsg);

            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                log.setOperatorIp(resolveIp(request));
                log.setOperatorUa(request.getHeader("User-Agent"));
            }
            this.save(log);
        } catch (Exception e) {
            // 审计日志失败不应影响主业务
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    private String resolveIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
