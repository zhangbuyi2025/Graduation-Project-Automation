package com.example.edu_digization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edu_digization.dto.ArchiveSubmitRequest;
import com.example.edu_digization.dto.ReviewDTO;
import com.example.edu_digization.entity.Archive;
import com.example.edu_digization.entity.User;
import com.example.edu_digization.vo.ArchiveDetailVO;
import com.example.edu_digization.vo.ArchiveListVO;

import java.util.List;

/**
 * 教学档案 Service
 */
public interface IArchiveService extends IService<Archive> {

    /**
     * 教师提交 / 重新提交档案（含附件落盘后的元数据）
     *
     * @return 提交后的档案列表项
     */
    ArchiveListVO submit(ArchiveSubmitRequest request);

    /**
     * 我的档案列表
     */
    List<ArchiveListVO> listMy(Long teacherId);

    /**
     * 档案详情（当前版本附件 + 全部审核记录）
     */
    ArchiveDetailVO detail(Long archiveId);

    /**
     * 一级审核：教研室主任初审
     */
    void firstReview(ReviewDTO dto, User reviewer);

    /**
     * 二级审核：教务处中间审核
     */
    void middleReview(ReviewDTO dto, User reviewer);

    /**
     * 三级审核：教学院长终审
     */
    void secondReview(ReviewDTO dto, User reviewer);

    /**
     * 归档/档案列表查询（支持学院、教研室、学期、状态、类型、关键字筛选）
     */
    List<ArchiveListVO> listArchived(Long collegeId, Long deptId, Long semesterId,
                                     String status, String archiveType, String keyword);

    /**
     * 逻辑删除档案（仅允许本人在待初审 / 已驳回状态删除）
     */
    void logicDelete(Long archiveId, User operator);
}
