package com.example.edu_digization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.edu_digization.entity.Attachment;
import com.example.edu_digization.mapper.AttachmentMapper;
import com.example.edu_digization.service.IAttachmentService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 附件 Service 实现
 */
@Service
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper, Attachment> implements IAttachmentService {

    @Override
    public List<Attachment> listByArchiveAndVersion(Long archiveId, Integer uploadVersion) {
        return this.list(new LambdaQueryWrapper<Attachment>()
                .eq(Attachment::getArchiveId, archiveId)
                .eq(Attachment::getUploadVersion, uploadVersion)
                .orderByAsc(Attachment::getSlotIndex));
    }
}
