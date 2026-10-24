package com.example.edu_digization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.edu_digization.entity.Attachment;

import java.util.List;

/**
 * 附件 Service
 */
public interface IAttachmentService extends IService<Attachment> {

    /**
     * 查询某档案指定版本的附件（按槽位升序）
     */
    List<Attachment> listByArchiveAndVersion(Long archiveId, Integer uploadVersion);
}
