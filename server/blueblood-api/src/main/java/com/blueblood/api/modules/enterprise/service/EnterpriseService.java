package com.blueblood.api.modules.enterprise.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.enterprise.dto.EnterpriseApplicationRequest;
import com.blueblood.api.modules.enterprise.dto.HackathonRequest;
import com.blueblood.api.modules.enterprise.dto.JobRequest;
import com.blueblood.api.modules.enterprise.entity.EnterpriseApplication;
import com.blueblood.api.modules.enterprise.entity.Hackathon;
import com.blueblood.api.modules.enterprise.entity.Job;
import com.blueblood.api.modules.enterprise.mapper.EnterpriseApplicationMapper;
import com.blueblood.api.modules.enterprise.mapper.HackathonMapper;
import com.blueblood.api.modules.enterprise.mapper.JobMapper;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.UserMapper;
import com.blueblood.api.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 企业用户端服务：企业认证申请 + 岗位发布 + 黑客松发布。
 * 发布类接口统一校验 user_type=enterprise，否则抛 FORBIDDEN。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseService {

    private final EnterpriseApplicationMapper applicationMapper;
    private final JobMapper jobMapper;
    private final HackathonMapper hackathonMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    // ============================== 企业认证申请 ==============================

    @Transactional
    public Long submitApplication(EnterpriseApplicationRequest req) {
        Long userId = SecurityUtils.currentUserId();
        User user = getUser(userId);

        // 已是企业用户直接拒绝
        if ("enterprise".equalsIgnoreCase(user.getUserType())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "已是企业用户，无需重复申请");
        }
        // 已有 PENDING/APPROVED 申请则拒绝
        Long exists = applicationMapper.selectCount(new LambdaQueryWrapper<EnterpriseApplication>()
                .eq(EnterpriseApplication::getUserId, userId)
                .in(EnterpriseApplication::getStatus, "PENDING", "APPROVED")
                .isNull(EnterpriseApplication::getDeletedAt));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "已有待审核或已通过的企业申请");
        }

        EnterpriseApplication app = new EnterpriseApplication();
        app.setUserId(userId);
        app.setCompanyName(req.getCompanyName());
        app.setCreditCode(req.getCreditCode());
        app.setLicenseUrl(req.getLicenseUrl() == null ? "" : req.getLicenseUrl());
        app.setContactName(req.getContactName());
        app.setContactPhone(req.getContactPhone());
        app.setStatus("PENDING");
        applicationMapper.insert(app);
        return app.getId();
    }

    public EnterpriseApplication myApplication() {
        Long userId = SecurityUtils.currentUserId();
        return applicationMapper.selectOne(new LambdaQueryWrapper<EnterpriseApplication>()
                .eq(EnterpriseApplication::getUserId, userId)
                .isNull(EnterpriseApplication::getDeletedAt)
                .orderByDesc(EnterpriseApplication::getCreatedAt)
                .last("LIMIT 1"));
    }

    // ============================== 岗位 ==============================

    @Transactional
    public Long publishJob(JobRequest req) {
        Long userId = SecurityUtils.currentUserId();
        ensureEnterprise(userId);

        Job job = new Job();
        applyJobFields(job, req);
        job.setStatus(StringUtils.hasText(req.getStatus()) ? req.getStatus().toUpperCase() : "ACTIVE");
        job.setPublishedBy(userId);
        jobMapper.insert(job);
        return job.getId();
    }

    public List<Job> myJobs() {
        Long userId = SecurityUtils.currentUserId();
        return jobMapper.selectList(new LambdaQueryWrapper<Job>()
                .eq(Job::getPublishedBy, userId)
                .isNull(Job::getDeletedAt)
                .orderByDesc(Job::getCreatedAt));
    }

    @Transactional
    public void updateJob(Long jobId, JobRequest req) {
        Long userId = SecurityUtils.currentUserId();
        ensureEnterprise(userId);
        Job job = getOwnedJob(jobId, userId);
        applyJobFields(job, req);
        if (StringUtils.hasText(req.getStatus())) {
            job.setStatus(req.getStatus().toUpperCase());
        }
        jobMapper.updateById(job);
    }

    @Transactional
    public void deleteJob(Long jobId) {
        Long userId = SecurityUtils.currentUserId();
        ensureEnterprise(userId);
        Job job = getOwnedJob(jobId, userId);
        Job patch = new Job();
        patch.setId(job.getId());
        patch.setDeletedAt(LocalDateTime.now());
        jobMapper.updateById(patch);
    }

    // ============================== 黑客松 ==============================

    @Transactional
    public Long publishHackathon(HackathonRequest req) {
        Long userId = SecurityUtils.currentUserId();
        ensureEnterprise(userId);

        Hackathon hackathon = new Hackathon();
        applyHackathonFields(hackathon, req);
        hackathon.setStatus(StringUtils.hasText(req.getStatus()) ? req.getStatus() : "signup");
        hackathon.setCurrentTeams(req.getCurrentTeams() == null ? 0 : req.getCurrentTeams());
        hackathon.setPublishedBy(userId);
        hackathonMapper.insert(hackathon);
        return hackathon.getId();
    }

    public List<Hackathon> myHackathons() {
        Long userId = SecurityUtils.currentUserId();
        return hackathonMapper.selectList(new LambdaQueryWrapper<Hackathon>()
                .eq(Hackathon::getPublishedBy, userId)
                .isNull(Hackathon::getDeletedAt)
                .orderByDesc(Hackathon::getCreatedAt));
    }

    @Transactional
    public void updateHackathon(Long id, HackathonRequest req) {
        Long userId = SecurityUtils.currentUserId();
        ensureEnterprise(userId);
        Hackathon hackathon = getOwnedHackathon(id, userId);
        applyHackathonFields(hackathon, req);
        if (StringUtils.hasText(req.getStatus())) {
            hackathon.setStatus(req.getStatus());
        }
        if (req.getCurrentTeams() != null) {
            hackathon.setCurrentTeams(req.getCurrentTeams());
        }
        hackathonMapper.updateById(hackathon);
    }

    @Transactional
    public void deleteHackathon(Long id) {
        Long userId = SecurityUtils.currentUserId();
        ensureEnterprise(userId);
        Hackathon hackathon = getOwnedHackathon(id, userId);
        Hackathon patch = new Hackathon();
        patch.setId(hackathon.getId());
        patch.setDeletedAt(LocalDateTime.now());
        hackathonMapper.updateById(patch);
    }

    // ============================== 私有工具 ==============================

    /** 发布类操作前置校验：当前用户必须是企业用户。判 user_type 而非 role，故不用 @PreAuthorize。 */
    private void ensureEnterprise(Long userId) {
        User user = getUser(userId);
        if (!"enterprise".equalsIgnoreCase(user.getUserType())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅企业用户可发布");
        }
    }

    private User getUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private Job getOwnedJob(Long jobId, Long userId) {
        Job job = jobMapper.selectById(jobId);
        if (job == null || job.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "岗位不存在");
        }
        if (!userId.equals(job.getPublishedBy())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作他人岗位");
        }
        return job;
    }

    private Hackathon getOwnedHackathon(Long id, Long userId) {
        Hackathon hackathon = hackathonMapper.selectById(id);
        if (hackathon == null || hackathon.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "黑客松不存在");
        }
        if (!userId.equals(hackathon.getPublishedBy())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作他人黑客松");
        }
        return hackathon;
    }

    private void applyJobFields(Job job, JobRequest req) {
        job.setTitle(req.getTitle());
        job.setCompany(req.getCompany());
        job.setCompanyLogo(req.getCompanyLogo() == null ? "" : req.getCompanyLogo());
        job.setLocation(req.getLocation() == null ? "" : req.getLocation());
        job.setSalary(req.getSalary() == null ? "" : req.getSalary());
        job.setType(req.getType() == null ? "" : req.getType());
        job.setDescription(req.getDescription() == null ? "" : req.getDescription());
        job.setContact(req.getContact() == null ? "" : req.getContact());
        job.setTags(toJson(req.getTags(), "岗位标签"));
        job.setRequirements(toJson(req.getRequirements(), "岗位要求"));
    }

    private void applyHackathonFields(Hackathon hackathon, HackathonRequest req) {
        hackathon.setTitle(req.getTitle());
        hackathon.setDescription(req.getDescription() == null ? "" : req.getDescription());
        hackathon.setCoverImage(req.getCoverImage() == null ? "" : req.getCoverImage());
        hackathon.setPrizePool(req.getPrizePool());
        hackathon.setStartTime(req.getStartTime());
        hackathon.setEndTime(req.getEndTime());
        hackathon.setSignupDeadline(req.getSignupDeadline());
        hackathon.setLocation(req.getLocation() == null ? "" : req.getLocation());
        hackathon.setMaxTeams(req.getMaxTeams() == null ? 0 : req.getMaxTeams());
    }

    private String toJson(List<String> list, String label) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.warn("序列化{}失败", label, e);
            return "[]";
        }
    }
}
