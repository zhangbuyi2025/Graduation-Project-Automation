package com.example.edu_digization.controller;

import com.example.edu_digization.common.Result;
import com.example.edu_digization.dto.ArchiveSubmitRequest;
import com.example.edu_digization.dto.StoredFile;
import com.example.edu_digization.entity.Attachment;
import com.example.edu_digization.entity.User;
import com.example.edu_digization.service.IArchiveService;
import com.example.edu_digization.service.IAttachmentService;
import com.example.edu_digization.service.ICourseInstanceService;
import com.example.edu_digization.service.IAuditLogService;
import com.example.edu_digization.service.infrastructure.ArchiveExportService;
import com.example.edu_digization.service.infrastructure.FileStorageService;
import com.example.edu_digization.utils.SecurityUtils;
import com.example.edu_digization.vo.ArchiveDetailVO;
import com.example.edu_digization.vo.ArchiveListVO;
import com.example.edu_digization.vo.TeachingTaskVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 教学档案控制器：教学任务、提交、查询、删除、附件下载
 */
@RestController
@RequestMapping("/api/work")
@RequiredArgsConstructor
public class ArchiveController {

    private final IArchiveService archiveService;
    private final IAttachmentService attachmentService;
    private final ICourseInstanceService courseInstanceService;
    private final IAuditLogService auditLogService;
    private final ArchiveExportService archiveExportService;
    private final FileStorageService fileStorageService;
    private final SecurityUtils securityUtils;
    private final ObjectMapper objectMapper;

    /**
     * 当前（或指定）学期的教学任务列表
     */
    @GetMapping("/task")
    public Result<List<TeachingTaskVO>> tasks(
            @RequestParam(value = "semesterId", required = false) Long semesterId) {
        User teacher = securityUtils.getCurrentUser();
        return Result.success(courseInstanceService.listTeachingTasks(teacher.getUserId(), semesterId));
    }

    /**
     * 提交 / 重提档案（multipart）
     * 参数：files（PDF 数组）、instanceId、deptId、archiveType、
     *      slotIndexes（逗号分隔）、slotNames（JSON 数组）、archiveId（重提时）
     */
    @PostMapping("/archive/submit")
    public Result<ArchiveListVO> submit(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("instanceId") Long instanceId,
            @RequestParam("deptId") Long deptId,
            @RequestParam("archiveType") String archiveType,
            @RequestParam(value = "slotIndexes", required = false) String slotIndexes,
            @RequestParam(value = "slotNames", required = false) String slotNames,
            @RequestParam(value = "archiveId", required = false) Long archiveId) {

        User teacher = securityUtils.getCurrentUser();

        List<Integer> indexList = parseIntegerList(slotIndexes);
        List<String> nameList = parseStringList(slotNames);

        List<StoredFile> storedFiles = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            Integer slotIndex = i < indexList.size() ? indexList.get(i) : i + 1;
            String slotName = i < nameList.size() ? nameList.get(i) : null;
            storedFiles.add(fileStorageService.store(files[i], slotIndex, slotName));
        }

        ArchiveSubmitRequest request = new ArchiveSubmitRequest();
        request.setInstanceId(instanceId);
        request.setTeacherId(teacher.getUserId());
        request.setDeptId(deptId);
        request.setArchiveType(archiveType);
        request.setArchiveId(archiveId);
        request.setFiles(storedFiles);

        return Result.success(archiveService.submit(request));
    }

    /** 我的档案列表 */
    @GetMapping("/archive/mine")
    public Result<List<ArchiveListVO>> mine() {
        User teacher = securityUtils.getCurrentUser();
        return Result.success(archiveService.listMy(teacher.getUserId()));
    }

    /** 归档/档案列表（按角色范围，支持筛选） */
    @GetMapping("/archive/archived")
    public Result<List<ArchiveListVO>> archived(
            @RequestParam(value = "collegeId", required = false) Long collegeId,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "semesterId", required = false) Long semesterId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "archiveType", required = false) String archiveType,
            @RequestParam(value = "keyword", required = false) String keyword) {
        User current = securityUtils.getCurrentUser();
        // 数据范围收口：教研室主任只能看本教研室，院长只能看本学院
        if ("dean_chair".equals(current.getRole())) {
            deptId = current.getDeptId();
        } else if ("dean".equals(current.getRole())) {
            collegeId = current.getCollegeId();
        }
        return Result.success(archiveService.listArchived(
                collegeId, deptId, semesterId, status, archiveType, keyword));
    }

    /**
     * 导出归档清单 Excel（主任/院长，数据范围按角色收口）
     */
    @GetMapping("/archive/export")
    public void export(
            @RequestParam(value = "semesterId", required = false) Long semesterId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "archiveType", required = false) String archiveType,
            HttpServletResponse response) {
        User current = securityUtils.getCurrentUser();
        Long collegeId = null;
        Long deptId = null;
        if ("dean_chair".equals(current.getRole())) {
            deptId = current.getDeptId();
        } else if ("dean".equals(current.getRole())) {
            collegeId = current.getCollegeId();
        }
        List<ArchiveListVO> data = archiveService.listArchived(
                collegeId, deptId, semesterId, status, archiveType, null);
        auditLogService.record(current.getUserId(), current.getRole(), "EXPORT",
                "ARCHIVE", null, null, "SUCCESS", null);
        archiveExportService.export(response, data, "教学档案归档清单.xlsx");
    }

    /** 档案详情 */
    @GetMapping("/archive/{id}")
    public Result<ArchiveDetailVO> detail(@PathVariable("id") Long id) {
        return Result.success(archiveService.detail(id));
    }

    /** 逻辑删除档案 */
    @DeleteMapping("/archive/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        User operator = securityUtils.getCurrentUser();
        archiveService.logicDelete(id, operator);
        return Result.success();
    }

    /** 附件下载 / 预览（inline） */
    @GetMapping("/attachment/{id}/download")
    public ResponseEntity<FileSystemResource> downloadAttachment(
            @PathVariable("id") Long id,
            @RequestParam(value = "inline", required = false, defaultValue = "false") boolean inline) {
        Attachment attachment = attachmentService.getById(id);
        if (attachment == null) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(attachment.getStoragePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(file);
        String encodedName = URLEncoder.encode(attachment.getOriginalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        String disposition = (inline ? "inline" : "attachment") + ";filename*=UTF-8''" + encodedName;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(file.length())
                .body(resource);
    }

    // ===================== 私有辅助 =====================

    private List<Integer> parseIntegerList(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        List<Integer> result = new ArrayList<>();
        for (String s : text.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) {
                result.add(Integer.parseInt(t));
            }
        }
        return result;
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            // 兼容前端传入 JSON 数组
            if (json.trim().startsWith("[")) {
                return objectMapper.readValue(json, new TypeReference<List<String>>() {
                });
            }
            return new ArrayList<>(Arrays.asList(json.split("\\|")));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
