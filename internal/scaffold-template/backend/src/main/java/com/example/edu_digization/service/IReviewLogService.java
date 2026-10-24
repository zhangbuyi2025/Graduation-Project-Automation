package com.example.edu_digization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edu_digization.entity.ReviewLog;
import com.example.edu_digization.vo.ReviewLogVO;

import java.util.List;

/**
 * 审核记录 Service
 */
public interface IReviewLogService extends IService<ReviewLog> {

    /**
     * 审核记录列表，支持多条件筛选
     */
    List<ReviewLogVO> listLogs(Long collegeId, Long deptId, Long semesterId,
                               Integer reviewLevel, String result, String keyword);
}
