package com.blueblood.api.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.admin.dto.AdminJobQuery;
import com.blueblood.api.modules.admin.dto.AdminJobVO;
import com.blueblood.api.modules.enterprise.dto.JobRequest;
import com.blueblood.api.modules.enterprise.entity.Job;
import com.blueblood.api.modules.enterprise.mapper.JobMapper;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台岗位管理：列表(筛选分页，含发布人昵称)、新增/编辑/删除。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminJobService {

    private final JobMapper jobMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public PageResult<AdminJobVO> page(AdminJobQuery query) {
        Page<Job> page = query.toPage();
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<Job>()
                .isNull(Job::getDeletedAt)
                .orderByDesc(Job::getCreatedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(Job::getTitle, kw).or().like(Job::getCompany, kw));
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Job::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getType())) {
            wrapper.eq(Job::getType, query.getType());
        }
        Page<Job> result = jobMapper.selectPage(page, wrapper);
        Map<Long, String> publisherMap = loadNicknames(result.getRecords());
        return PageResult.of(result.convert(j -> toVO(j, publisherMap.get(j.getPublishedBy()))));
    }

    public AdminJobVO detail(Long id) {
        Job job = getJob(id);
        User u = job.getPublishedBy() == null ? null : userMapper.selectById(job.getPublishedBy());
        return toVO(job, u == null ? null : u.getNickname());
    }

    @Transactional
    public Long create(JobRequest req) {
        Job job = new Job();
        applyFields(job, req);
        job.setStatus(StringUtils.hasText(req.getStatus()) ? req.getStatus().toUpperCase() : "ACTIVE");
        jobMapper.insert(job);
        return job.getId();
    }

    @Transactional
    public void update(Long id, JobRequest req) {
        Job job = getJob(id);
        applyFields(job, req);
        if (StringUtils.hasText(req.getStatus())) {
            job.setStatus(req.getStatus().toUpperCase());
        }
        jobMapper.updateById(job);
    }

    @Transactional
    public void delete(Long id) {
        Job job = getJob(id);
        Job patch = new Job();
        patch.setId(job.getId());
        patch.setDeletedAt(LocalDateTime.now());
        jobMapper.updateById(patch);
    }

    private Job getJob(Long id) {
        Job job = jobMapper.selectById(id);
        if (job == null || job.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "岗位不存在");
        }
        return job;
    }

    private void applyFields(Job job, JobRequest req) {
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

    private Map<Long, String> loadNicknames(List<Job> records) {
        Map<Long, String> map = new HashMap<>();
        if (records == null || records.isEmpty()) {
            return map;
        }
        List<Long> userIds = records.stream()
                .map(Job::getPublishedBy)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        for (Long uid : userIds) {
            User u = userMapper.selectById(uid);
            if (u != null) {
                map.put(uid, u.getNickname());
            }
        }
        return map;
    }

    private AdminJobVO toVO(Job j, String publisherNickname) {
        AdminJobVO vo = new AdminJobVO();
        vo.setId(j.getId());
        vo.setTitle(j.getTitle());
        vo.setCompany(j.getCompany());
        vo.setCompanyLogo(j.getCompanyLogo());
        vo.setLocation(j.getLocation());
        vo.setSalary(j.getSalary());
        vo.setType(j.getType());
        vo.setTags(j.getTags());
        vo.setDescription(j.getDescription());
        vo.setRequirements(j.getRequirements());
        vo.setContact(j.getContact());
        vo.setStatus(j.getStatus());
        vo.setPublishedBy(j.getPublishedBy());
        vo.setPublisherNickname(publisherNickname);
        vo.setCreatedAt(j.getCreatedAt());
        vo.setUpdatedAt(j.getUpdatedAt());
        return vo;
    }
}
