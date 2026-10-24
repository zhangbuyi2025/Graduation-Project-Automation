package com.example.edu_digization.controller;

import com.example.edu_digization.common.ArchiveStatus;
import com.example.edu_digization.common.Result;
import com.example.edu_digization.dto.ReviewDTO;
import com.example.edu_digization.entity.User;
import com.example.edu_digization.service.IArchiveService;
import com.example.edu_digization.service.IReviewLogService;
import com.example.edu_digization.utils.SecurityUtils;
import com.example.edu_digization.vo.ArchiveListVO;
import com.example.edu_digization.vo.ReviewLogVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 审核控制器：待审列表、初审、终审、审批记录
 * 教务处中间审核见 controller/admin/MiddleReviewController
 */
@RestController
@RequestMapping("/api/work")
@RequiredArgsConstructor
public class ReviewController {

    private final IArchiveService archiveService;
    private final IReviewLogService reviewLogService;
    private final SecurityUtils securityUtils;

    /**
     * 待审核列表（按角色自动匹配）：
     * 教研室主任 → 待初审 PENDING（本教研室）
     * 教学院长   → 待终审 MIDDLE_REVIEW（本学院）
     */
    @GetMapping("/review/pending")
    public Result<List<ArchiveListVO>> pending(
            @RequestParam(value = "semesterId", required = false) Long semesterId) {
        User current = securityUtils.getCurrentUser();
        List<ArchiveListVO> list;

        switch (current.getRole()) {
            case "dean_chair":
                list = archiveService.listArchived(null, current.getDeptId(), semesterId,
                        ArchiveStatus.PENDING, null, null);
                break;
            case "dean":
                list = archiveService.listArchived(current.getCollegeId(), null, semesterId,
                        ArchiveStatus.MIDDLE_REVIEW, null, null);
                break;
            default:
                list = Collections.emptyList();
        }
        return Result.success(list);
    }

    /**
     * 一级审核：教研室主任初审
     */
    @PostMapping("/review/first")
    @PreAuthorize("hasRole('DEAN_CHAIR')")
    public Result<Void> first(@Valid @RequestBody ReviewDTO dto) {
        User reviewer = securityUtils.getCurrentUser();
        archiveService.firstReview(dto, reviewer);
        return Result.success();
    }

    /**
     * 三级审核：教学院长终审
     */
    @PostMapping("/review/second")
    @PreAuthorize("hasRole('DEAN')")
    public Result<Void> second(@Valid @RequestBody ReviewDTO dto) {
        User reviewer = securityUtils.getCurrentUser();
        archiveService.secondReview(dto, reviewer);
        return Result.success();
    }

    /**
     * 审批记录查询：主任按本教研室、院长按本学院收口
     */
    @GetMapping("/review/logs")
    public Result<List<ReviewLogVO>> logs(
            @RequestParam(value = "semesterId", required = false) Long semesterId,
            @RequestParam(value = "reviewLevel", required = false) Integer reviewLevel,
            @RequestParam(value = "result", required = false) String result,
            @RequestParam(value = "keyword", required = false) String keyword) {
        User current = securityUtils.getCurrentUser();

        Long collegeId = null;
        Long deptId = null;
        if ("dean_chair".equals(current.getRole())) {
            deptId = current.getDeptId();
        } else if ("dean".equals(current.getRole())) {
            collegeId = current.getCollegeId();
        }

        return Result.success(reviewLogService.listLogs(
                collegeId, deptId, semesterId, reviewLevel, result, keyword));
    }
}
