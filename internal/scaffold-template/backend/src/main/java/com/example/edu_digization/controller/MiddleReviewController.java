package com.example.edu_digization.controller;

import com.example.edu_digization.common.ArchiveStatus;
import com.example.edu_digization.common.Result;
import com.example.edu_digization.dto.ReviewDTO;
import com.example.edu_digization.entity.User;
import com.example.edu_digization.service.IArchiveService;
import com.example.edu_digization.utils.SecurityUtils;
import com.example.edu_digization.vo.ArchiveListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 教务处中间审核控制器（三级审核的第二级）
 */
@RestController
@RequestMapping("/api/admin/middle-review")
@RequiredArgsConstructor
public class MiddleReviewController {

    private final IArchiveService archiveService;
    private final SecurityUtils securityUtils;

    /**
     * 待中间审核列表（status = FIRST_REVIEW，已过初审），支持学院/教研室/学期筛选
     */
    @GetMapping("/pending")
    public Result<List<ArchiveListVO>> pending(
            @RequestParam(value = "collegeId", required = false) Long collegeId,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "semesterId", required = false) Long semesterId) {
        return Result.success(archiveService.listArchived(
                collegeId, deptId, semesterId, ArchiveStatus.FIRST_REVIEW, null, null));
    }

    /**
     * 执行中间审核
     * 请求体：{ result: APPROVED/REJECTED, comment, rejectedSlots }
     */
    @PostMapping("/{id}")
    public Result<Void> review(@PathVariable("id") Long id, @RequestBody ReviewDTO body) {
        User reviewer = securityUtils.getCurrentUser();
        body.setArchiveId(id);
        archiveService.middleReview(body, reviewer);
        return Result.success();
    }
}
