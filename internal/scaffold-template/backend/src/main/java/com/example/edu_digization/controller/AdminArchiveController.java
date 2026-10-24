package com.example.edu_digization.controller;

import com.example.edu_digization.common.Result;
import com.example.edu_digization.service.IArchiveService;
import com.example.edu_digization.service.IAuditLogService;
import com.example.edu_digization.service.infrastructure.ArchiveExportService;
import com.example.edu_digization.vo.ArchiveListVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 教务处归档总览控制器：跨学院查询 + Excel 导出
 */
@RestController
@RequestMapping("/api/admin/archive")
@RequiredArgsConstructor
public class AdminArchiveController {

    private final IArchiveService archiveService;
    private final ArchiveExportService archiveExportService;
    private final IAuditLogService auditLogService;

    /**
     * 全校档案列表（支持学院、教研室、学期、状态、类型、关键字筛选）
     */
    @GetMapping
    public Result<List<ArchiveListVO>> list(
            @RequestParam(value = "collegeId", required = false) Long collegeId,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "semesterId", required = false) Long semesterId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "archiveType", required = false) String archiveType,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(archiveService.listArchived(
                collegeId, deptId, semesterId, status, archiveType, keyword));
    }

    /**
     * 导出归档清单 Excel
     */
    @GetMapping("/export")
    public void export(
            @RequestParam(value = "collegeId", required = false) Long collegeId,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "semesterId", required = false) Long semesterId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "archiveType", required = false) String archiveType,
            @RequestParam(value = "keyword", required = false) String keyword,
            HttpServletResponse response) {
        List<ArchiveListVO> data = archiveService.listArchived(
                collegeId, deptId, semesterId, status, archiveType, keyword);
        auditLogService.record(null, "admin", "EXPORT", "ARCHIVE", null, null, "SUCCESS", null);
        archiveExportService.export(response, data, "教学档案归档清单.xlsx");
    }
}
