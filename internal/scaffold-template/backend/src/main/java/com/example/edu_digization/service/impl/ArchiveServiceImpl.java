package com.example.edu_digization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edu_digization.common.ArchiveStatus;
import com.example.edu_digization.common.ArchiveType;
import com.example.edu_digization.dto.ArchiveSubmitRequest;
import com.example.edu_digization.dto.ReviewDTO;
import com.example.edu_digization.dto.StoredFile;
import com.example.edu_digization.entity.Archive;
import com.example.edu_digization.entity.Attachment;
import com.example.edu_digization.entity.ReviewLog;
import com.example.edu_digization.entity.User;
import com.example.edu_digization.exception.BusinessException;
import com.example.edu_digization.mapper.ArchiveMapper;
import com.example.edu_digization.service.IArchiveService;
import com.example.edu_digization.service.IAttachmentService;
import com.example.edu_digization.service.IAuditLogService;
import com.example.edu_digization.service.IReviewLogService;
import com.example.edu_digization.vo.ArchiveDetailVO;
import com.example.edu_digization.vo.ArchiveListVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 教学档案 Service 实现
 */
@Service
@RequiredArgsConstructor
public class ArchiveServiceImpl extends ServiceImpl<ArchiveMapper, Archive> implements IArchiveService {

    private final IAttachmentService attachmentService;
    private final IReviewLogService reviewLogService;
    private final IAuditLogService auditLogService;

    /** 课前档案固定槽位数 */
    private static final int PRE_SLOT_COUNT = 5;

    // ===================== 提交 / 重提 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArchiveListVO submit(ArchiveSubmitRequest request) {
        // 1. 基础校验
        if (request.getInstanceId() == null) {
            throw new BusinessException("缺少课程实例信息");
        }
        if (request.getTeacherId() == null) {
            throw new BusinessException("缺少教师信息");
        }
        if (request.getDeptId() == null) {
            throw new BusinessException("请确认所属教研室");
        }
        String type = request.getArchiveType();
        if (!ArchiveType.PRE.equals(type) && !ArchiveType.POST.equals(type)) {
            throw new BusinessException("档案类型不合法");
        }
        List<StoredFile> files = request.getFiles();
        if (files == null || files.isEmpty()) {
            throw new BusinessException("请至少上传一个 PDF 附件");
        }
        if (ArchiveType.PRE.equals(type) && files.size() != PRE_SLOT_COUNT) {
            throw new BusinessException("课前档案必须提交 5 个 PDF 附件");
        }
        validateSlots(files);

        // 2. 查找已有档案（显式 ID 或按实例+类型）
        Archive archive = null;
        if (request.getArchiveId() != null) {
            archive = this.getById(request.getArchiveId());
        }
        if (archive == null) {
            archive = this.getOne(new LambdaQueryWrapper<Archive>()
                    .eq(Archive::getInstanceId, request.getInstanceId())
                    .eq(Archive::getArchiveType, type)
                    .eq(Archive::getIsDeleted, 0)
                    .last("LIMIT 1"));
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isResubmit = archive != null;

        if (isResubmit) {
            // 只有已驳回的档案才允许重提
            if (!ArchiveStatus.REJECTED.equals(archive.getStatus())
                    && !ArchiveStatus.NOT_SUBMITTED.equals(archive.getStatus())) {
                throw new BusinessException("当前档案状态不允许重新提交");
            }
            int newVersion = archive.getVersionNo() + 1;
            archive.setVersionNo(newVersion);
            archive.setStatus(ArchiveStatus.PENDING);
            archive.setDeptId(request.getDeptId());
            archive.setLastResubmitTime(now);
            this.updateById(archive);
            saveAttachments(archive.getArchiveId(), request.getTeacherId(), files, newVersion);
            auditLogService.record(request.getTeacherId(), null, "RESUBMIT",
                    "ARCHIVE", archive.getArchiveId(),
                    "{\"version\":" + newVersion + "}", "SUCCESS", null);
        } else {
            archive = new Archive();
            archive.setInstanceId(request.getInstanceId());
            archive.setTeacherId(request.getTeacherId());
            archive.setDeptId(request.getDeptId());
            archive.setArchiveType(type);
            archive.setStatus(ArchiveStatus.PENDING);
            archive.setVersionNo(1);
            archive.setSubmitTime(now);
            archive.setIsDeleted(0);
            this.save(archive);
            saveAttachments(archive.getArchiveId(), request.getTeacherId(), files, 1);
            auditLogService.record(request.getTeacherId(), null, "SUBMIT",
                    "ARCHIVE", archive.getArchiveId(), null, "SUCCESS", null);
        }

        return this.baseMapper.selectArchiveVOById(archive.getArchiveId());
    }

    private void saveAttachments(Long archiveId, Long uploaderId, List<StoredFile> files, int version) {
        LocalDateTime now = LocalDateTime.now();
        for (StoredFile f : files) {
            Attachment att = new Attachment();
            att.setArchiveId(archiveId);
            att.setSlotIndex(f.getSlotIndex());
            att.setSlotName(f.getSlotName());
            att.setOriginalName(f.getOriginalName());
            att.setStorageName(f.getStorageName());
            att.setStoragePath(f.getStoragePath());
            att.setFileExt(f.getFileExt());
            att.setFileSize(f.getFileSize());
            att.setFileHash(f.getFileHash());
            att.setMimeType(f.getMimeType());
            att.setIsRejected(0);
            att.setUploadVersion(version);
            att.setUploaderId(uploaderId);
            att.setUploadTime(now);
            attachmentService.save(att);
        }
    }

    private void validateSlots(List<StoredFile> files) {
        Set<Integer> indexes = new HashSet<>();
        for (StoredFile f : files) {
            if (f.getSlotIndex() == null || f.getSlotIndex() < 1) {
                throw new BusinessException("附件槽位编号不合法");
            }
            if (!indexes.add(f.getSlotIndex())) {
                throw new BusinessException("附件槽位重复：槽位 " + f.getSlotIndex());
            }
        }
    }

    // ===================== 查询 =====================

    @Override
    public List<ArchiveListVO> listMy(Long teacherId) {
        return this.baseMapper.selectArchiveList(teacherId, null, null, null, null, null, null);
    }

    @Override
    public ArchiveDetailVO detail(Long archiveId) {
        ArchiveListVO archive = this.baseMapper.selectArchiveVOById(archiveId);
        if (archive == null) {
            throw new BusinessException(404, "档案不存在或已被删除");
        }
        Archive entity = this.getById(archiveId);
        List<Attachment> attachments =
                attachmentService.listByArchiveAndVersion(archiveId, entity.getVersionNo());
        List<ReviewLog> logs = reviewLogService.list(new LambdaQueryWrapper<ReviewLog>()
                .eq(ReviewLog::getArchiveId, archiveId)
                .orderByAsc(ReviewLog::getReviewTime));

        ArchiveDetailVO vo = new ArchiveDetailVO();
        vo.setArchive(archive);
        vo.setAttachments(attachments);
        vo.setReviewLogs(logs);
        return vo;
    }

    @Override
    public List<ArchiveListVO> listArchived(Long collegeId, Long deptId, Long semesterId,
                                            String status, String archiveType, String keyword) {
        return this.baseMapper.selectArchiveList(
                null, collegeId, deptId, semesterId, archiveType, status, keyword);
    }

    // ===================== 三级审核 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void firstReview(ReviewDTO dto, User reviewer) {
        Archive archive = loadForReview(dto.getArchiveId());
        if (!ArchiveStatus.PENDING.equals(archive.getStatus())) {
            throw new BusinessException("该档案当前不是待初审状态");
        }
        // 教研室主任只能审核本教研室档案
        if (reviewer.getDeptId() == null || !reviewer.getDeptId().equals(archive.getDeptId())) {
            throw new BusinessException(403, "只能审核本教研室的档案");
        }

        boolean approved = isApproved(dto);
        String after = approved ? ArchiveStatus.FIRST_REVIEW : ArchiveStatus.REJECTED;

        if (approved) {
            archive.setFirstReviewerId(reviewer.getUserId());
            archive.setFirstReviewAt(LocalDateTime.now());
        }
        archive.setStatus(after);
        this.updateById(archive);

        writeReviewLog(archive, 1, reviewer, dto, ArchiveStatus.PENDING, after);
        if (!approved) {
            markRejectedSlots(archive, dto);
        }
        auditLogService.record(reviewer.getUserId(), reviewer.getRole(), "FIRST_REVIEW",
                "ARCHIVE", archive.getArchiveId(),
                "{\"result\":\"" + (approved ? "APPROVED" : "REJECTED") + "\"}",
                "SUCCESS", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void middleReview(ReviewDTO dto, User reviewer) {
        Archive archive = loadForReview(dto.getArchiveId());
        if (!ArchiveStatus.FIRST_REVIEW.equals(archive.getStatus())) {
            throw new BusinessException("该档案当前不是待中间审核状态");
        }

        boolean approved = isApproved(dto);
        String after = approved ? ArchiveStatus.MIDDLE_REVIEW : ArchiveStatus.REJECTED;

        if (approved) {
            archive.setMiddleReviewerId(reviewer.getUserId());
            archive.setMiddleReviewAt(LocalDateTime.now());
        }
        archive.setStatus(after);
        this.updateById(archive);

        writeReviewLog(archive, 2, reviewer, dto, ArchiveStatus.FIRST_REVIEW, after);
        if (!approved) {
            markRejectedSlots(archive, dto);
        }
        auditLogService.record(reviewer.getUserId(), reviewer.getRole(), "MIDDLE_REVIEW",
                "ARCHIVE", archive.getArchiveId(),
                "{\"result\":\"" + (approved ? "APPROVED" : "REJECTED") + "\"}",
                "SUCCESS", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void secondReview(ReviewDTO dto, User reviewer) {
        Archive archive = loadForReview(dto.getArchiveId());
        if (!ArchiveStatus.MIDDLE_REVIEW.equals(archive.getStatus())) {
            throw new BusinessException("该档案当前不是待终审状态");
        }

        boolean approved = isApproved(dto);
        String after = approved ? ArchiveStatus.ARCHIVED : ArchiveStatus.REJECTED;

        if (approved) {
            archive.setSecondReviewerId(reviewer.getUserId());
            archive.setSecondReviewAt(LocalDateTime.now());
        }
        archive.setStatus(after);
        this.updateById(archive);

        writeReviewLog(archive, 3, reviewer, dto, ArchiveStatus.MIDDLE_REVIEW, after);
        if (!approved) {
            markRejectedSlots(archive, dto);
        }
        auditLogService.record(reviewer.getUserId(), reviewer.getRole(), "SECOND_REVIEW",
                "ARCHIVE", archive.getArchiveId(),
                "{\"result\":\"" + (approved ? "APPROVED" : "REJECTED") + "\"}",
                "SUCCESS", null);
    }

    // ===================== 删除 =====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logicDelete(Long archiveId, User operator) {
        Archive archive = loadForReview(archiveId);
        if (!archive.getTeacherId().equals(operator.getUserId())) {
            throw new BusinessException(403, "只能删除本人提交的档案");
        }
        if (!ArchiveStatus.PENDING.equals(archive.getStatus())
                && !ArchiveStatus.REJECTED.equals(archive.getStatus())) {
            throw new BusinessException("档案进入审核流程后不允许删除");
        }
        this.removeById(archiveId);
        auditLogService.record(operator.getUserId(), operator.getRole(), "DELETE",
                "ARCHIVE", archiveId, null, "SUCCESS", null);
    }

    // ===================== 私有辅助 =====================

    private Archive loadForReview(Long archiveId) {
        if (archiveId == null) {
            throw new BusinessException("档案 ID 不能为空");
        }
        Archive archive = this.getById(archiveId);
        if (archive == null || (archive.getIsDeleted() != null && archive.getIsDeleted() == 1)) {
            throw new BusinessException(404, "档案不存在或已被删除");
        }
        return archive;
    }

    private boolean isApproved(ReviewDTO dto) {
        String result = dto.getResult();
        if ("APPROVED".equals(result)) {
            return true;
        }
        if ("REJECTED".equals(result)) {
            if (dto.getComment() == null || dto.getComment().isBlank()) {
                throw new BusinessException("驳回时必须填写驳回理由");
            }
            return false;
        }
        throw new BusinessException("审核结果不合法（APPROVED / REJECTED）");
    }

    private void writeReviewLog(Archive archive, int level, User reviewer,
                                ReviewDTO dto, String before, String after) {
        ReviewLog log = new ReviewLog();
        log.setArchiveId(archive.getArchiveId());
        log.setReviewLevel(level);
        log.setReviewerId(reviewer.getUserId());
        log.setRoleSnapshot(reviewer.getRole());
        log.setResult("APPROVED".equals(dto.getResult()) ? "APPROVED" : "REJECTED");
        log.setComment(dto.getComment());
        log.setRejectedSlots(dto.getRejectedSlots());
        log.setReviewBeforeStatus(before);
        log.setReviewAfterStatus(after);
        log.setReviewTime(LocalDateTime.now());
        reviewLogService.save(log);
    }

    /**
     * 将被驳回的附件标记为驳回状态
     * rejectedSlots 为空表示一键驳回当前版本全部附件
     */
    private void markRejectedSlots(Archive archive, ReviewDTO dto) {
        List<Attachment> current =
                attachmentService.listByArchiveAndVersion(archive.getArchiveId(), archive.getVersionNo());

        List<Integer> rejectedIndexes = parseSlotIndexes(dto.getRejectedSlots());
        List<Attachment> targets;
        if (rejectedIndexes.isEmpty()) {
            targets = current; // 一键驳回
        } else {
            targets = current.stream()
                    .filter(a -> rejectedIndexes.contains(a.getSlotIndex()))
                    .collect(Collectors.toList());
        }

        for (Attachment a : targets) {
            a.setIsRejected(1);
            a.setRejectReason(dto.getComment());
            attachmentService.updateById(a);
        }
    }

    private List<Integer> parseSlotIndexes(String slots) {
        if (slots == null || slots.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return Arrays.stream(slots.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            throw new BusinessException("被驳回槽位格式不正确，应为如 1,3,5");
        }
    }
}
