package com.blueblood.api.modules.system.service;

import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.system.dto.FileVO;
import com.blueblood.api.modules.system.entity.SysFile;
import com.blueblood.api.modules.system.mapper.SysFileMapper;
import com.blueblood.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件服务：上传(本地)、查看、删除。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final SysFileMapper fileMapper;

    @Value("${upload.base-path:./uploads}")
    private String basePath;

    @Transactional
    public FileVO upload(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }

        String original = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "file";
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) {
            ext = original.substring(dot).toLowerCase();
        }

        // 按日期分目录，避免单目录文件过多
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MMdd"));
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path dir = Paths.get(basePath, dateDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException(ResultCode.OPERATION_FAILED, "文件保存失败");
        }

        String storedRel = dateDir + "/" + storedName;
        // URL 从当前请求动态推导(scheme+host+port+context-path)，避免端口/域名写死
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(storedRel)
                .toUriString();

        SysFile rec = new SysFile();
        rec.setOriginalName(original);
        rec.setStoredName(storedRel);
        rec.setUrl(url);
        rec.setMimeType(file.getContentType() == null ? "" : file.getContentType());
        rec.setSize(file.getSize());
        rec.setStorageType("local");
        rec.setBizType(bizType == null ? "" : bizType);
        rec.setUploadedBy(SecurityUtils.isLoggedIn() ? SecurityUtils.currentUserId() : null);
        fileMapper.insert(rec);

        return toVO(rec);
    }

    public FileVO info(Long id) {
        SysFile f = fileMapper.selectById(id);
        if (f == null || f.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文件不存在");
        }
        return toVO(f);
    }

    public void delete(Long id) {
        SysFile f = fileMapper.selectById(id);
        if (f == null || f.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "文件不存在");
        }
        SysFile patch = new SysFile();
        patch.setId(id);
        patch.setDeletedAt(LocalDateTime.now());
        fileMapper.updateById(patch);
        // 注意：仅软删数据库记录，物理文件保留（可由定时任务清理）
    }

    private FileVO toVO(SysFile f) {
        FileVO vo = new FileVO();
        vo.setId(f.getId());
        vo.setOriginalName(f.getOriginalName());
        vo.setUrl(f.getUrl());
        vo.setMimeType(f.getMimeType());
        vo.setSize(f.getSize());
        vo.setBizType(f.getBizType());
        vo.setCreatedAt(f.getCreatedAt());
        return vo;
    }
}
