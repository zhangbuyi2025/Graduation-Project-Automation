package com.example.edu_digization.service.infrastructure;

import com.example.edu_digization.dto.StoredFile;
import com.example.edu_digization.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.UUID;

/**
 * 文件存储服务：负责 PDF 校验、哈希计算与本地落盘
 * 存储根目录由配置 file.upload-dir 指定
 */
@Component
public class FileStorageService {

    /** 单个附件上限：50MB */
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    /** PDF 文件头魔数：%PDF- */
    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46, 0x2D};

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path rootPath;

    @PostConstruct
    public void init() {
        this.rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建文件存储目录：" + rootPath, e);
        }
    }

    /**
     * 校验并保存一个 PDF 附件
     *
     * @param file      上传的文件
     * @param slotIndex 槽位编号
     * @param slotName  槽位名称
     */
    public StoredFile store(MultipartFile file, Integer slotIndex, String slotName) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (originalName.isBlank()) {
            throw new BusinessException("文件名不能为空");
        }

        // 1. 扩展名校验
        String ext = getExtension(originalName);
        if (!"pdf".equalsIgnoreCase(ext)) {
            throw new BusinessException("仅支持 PDF 格式文件：" + originalName);
        }

        // 2. 大小校验
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("单个附件不能超过 50MB：" + originalName);
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("读取上传文件失败：" + originalName);
        }

        // 3. 文件头魔数校验（防止伪装）
        if (bytes.length < PDF_MAGIC.length || !startsWithMagic(bytes)) {
            throw new BusinessException("文件内容不是有效的 PDF（文件头校验失败）：" + originalName);
        }

        // 4. 计算 SHA-256
        String hash = sha256(bytes);

        // 5. 生成存储文件名并落盘
        String storageName = UUID.randomUUID().toString().replace("-", "") + ".pdf";
        Path target = rootPath.resolve(storageName);
        try {
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败：" + originalName);
        }

        StoredFile stored = new StoredFile();
        stored.setSlotIndex(slotIndex);
        stored.setSlotName(slotName);
        stored.setOriginalName(originalName);
        stored.setStorageName(storageName);
        stored.setStoragePath(target.toString());
        stored.setFileExt("pdf");
        stored.setFileSize(file.getSize());
        stored.setFileHash(hash);
        stored.setMimeType("application/pdf");
        return stored;
    }

    private boolean startsWithMagic(byte[] bytes) {
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (bytes[i] != PDF_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0 || idx == filename.length() - 1) {
            return "";
        }
        return filename.substring(idx + 1);
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new BusinessException("计算文件哈希失败");
        }
    }
}
