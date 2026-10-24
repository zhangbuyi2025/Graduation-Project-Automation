package com.example.edu_digization.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edu_digization.entity.ReviewLog;
import com.example.edu_digization.mapper.ReviewLogMapper;
import com.example.edu_digization.service.IReviewLogService;
import com.example.edu_digization.vo.ReviewLogVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 审核记录 Service 实现
 */
@Service
public class ReviewLogServiceImpl extends ServiceImpl<ReviewLogMapper, ReviewLog> implements IReviewLogService {

    @Override
    public List<ReviewLogVO> listLogs(Long collegeId, Long deptId, Long semesterId,
                                      Integer reviewLevel, String result, String keyword) {
        return this.baseMapper.selectReviewLogList(collegeId, deptId, semesterId,
                reviewLevel, result, keyword);
    }
}
