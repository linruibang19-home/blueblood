package com.blueblood.api.modules.assignment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.assignment.dto.AssignmentVO;
import com.blueblood.api.modules.assignment.dto.GradeAssignmentRequest;
import com.blueblood.api.modules.assignment.dto.SubmitAssignmentRequest;
import com.blueblood.api.modules.assignment.entity.Assignment;
import com.blueblood.api.modules.assignment.entity.AssignmentGrade;
import com.blueblood.api.modules.assignment.entity.AssignmentSubmission;
import com.blueblood.api.modules.assignment.mapper.AssignmentGradeMapper;
import com.blueblood.api.modules.assignment.mapper.AssignmentMapper;
import com.blueblood.api.modules.assignment.mapper.AssignmentSubmissionMapper;
import com.blueblood.api.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作业服务：课程作业列表、详情、提交、批改结果、管理员批改。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentMapper assignmentMapper;
    private final AssignmentSubmissionMapper submissionMapper;
    private final AssignmentGradeMapper gradeMapper;
    private final ObjectMapper objectMapper;

    private static final TypeReference<List<String>> LIST_TYPE = new TypeReference<>() {};

    // ============================== 查询 ==============================

    /** 课程作业分页（含当前用户状态/成绩） */
    public PageResult<AssignmentVO> pageByCourse(Long courseId, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100);
        Long userId = SecurityUtils.currentUserId();

        Page<Assignment> result = assignmentMapper.selectPage(new Page<>(p, size),
                new LambdaQueryWrapper<Assignment>()
                        .eq(Assignment::getCourseId, courseId)
                        .isNull(Assignment::getDeletedAt)
                        .orderByDesc(Assignment::getDeadline)
                        .orderByDesc(Assignment::getId));

        List<Assignment> assignments = result.getRecords();
        if (assignments.isEmpty()) {
            return PageResult.of(result.convert(a -> toListItem(a, null, null)));
        }

        List<Long> ids = assignments.stream().map(Assignment::getId).toList();
        Map<Long, AssignmentSubmission> subMap = loadSubmissions(ids, userId);
        List<Long> submittedIds = subMap.values().stream().map(AssignmentSubmission::getId).toList();
        Map<Long, AssignmentGrade> gradeMap = submittedIds.isEmpty()
                ? Collections.emptyMap() : loadGrades(submittedIds);

        return PageResult.of(result.convert(a -> {
            AssignmentSubmission sub = subMap.get(a.getId());
            AssignmentGrade grade = sub == null ? null : gradeMap.get(sub.getId());
            return toListItem(a, sub, grade);
        }));
    }

    /** 作业详情（含当前用户 submission + grade） */
    public AssignmentVO detail(Long assignmentId) {
        Assignment assignment = getActive(assignmentId);
        Long userId = SecurityUtils.currentUserId();

        AssignmentSubmission sub = findSubmission(assignmentId, userId);
        AssignmentGrade grade = sub == null ? null : findGradeBySubmission(sub.getId());

        AssignmentVO vo = new AssignmentVO();
        vo.setId(assignment.getId());
        vo.setTitle(assignment.getTitle());
        vo.setDescription(assignment.getDescription());
        vo.setDeadline(assignment.getDeadline());
        vo.setStatus(userStatus(sub, grade));
        if (grade != null) {
            vo.setScore(grade.getScore());
        }
        if (sub != null) {
            vo.setSubmission(toSubmissionDTO(sub));
        }
        if (grade != null) {
            vo.setGrade(toGradeDTO(grade));
        }
        return vo;
    }

    /** 批改结果：当前用户 submission + grade + 参考答案 */
    public AssignmentVO result(Long assignmentId) {
        Assignment assignment = getActive(assignmentId);
        Long userId = SecurityUtils.currentUserId();

        AssignmentSubmission sub = findSubmission(assignmentId, userId);
        if (sub == null) {
            // 未提交
            AssignmentVO vo = baseVO(assignment, userStatus(null, null));
            vo.setDescription(assignment.getDescription());
            return vo;
        }

        AssignmentGrade grade = findGradeBySubmission(sub.getId());
        AssignmentVO vo = baseVO(assignment, userStatus(sub, grade));
        vo.setDescription(assignment.getDescription());
        vo.setSubmission(toSubmissionDTO(sub));
        if (grade != null) {
            vo.setGrade(toGradeDTO(grade));
            vo.setScore(grade.getScore());
            // 已评分才揭示参考答案（防作弊）
            vo.setAnswer(assignment.getAnswer());
        }
        return vo;
    }

    // ============================== 提交 ==============================

    /**
     * 提交作业：upsert assignment_submission（content/attachments/submittedAt=now/status='submitted'）；
     * 已评分(graded)的不允许重复提交。
     */
    @Transactional
    public Map<String, Long> submit(Long assignmentId, SubmitAssignmentRequest request) {
        // 校验作业存在且未删除
        getActive(assignmentId);
        Long userId = SecurityUtils.currentUserId();

        AssignmentSubmission existing = findSubmission(assignmentId, userId);
        if (existing != null && "graded".equalsIgnoreCase(existing.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "作业已评分，不允许重复提交");
        }

        String attachmentsJson = toJson(request.getAttachments());
        LocalDateTime now = LocalDateTime.now();

        if (existing != null) {
            existing.setContent(request.getContent());
            existing.setAttachments(attachmentsJson);
            existing.setSubmittedAt(now);
            existing.setStatus("submitted");
            submissionMapper.updateById(existing);
            return Map.of("submissionId", existing.getId());
        }

        AssignmentSubmission sub = new AssignmentSubmission();
        sub.setAssignmentId(assignmentId);
        sub.setUserId(userId);
        sub.setContent(request.getContent());
        sub.setAttachments(attachmentsJson);
        sub.setSubmittedAt(now);
        sub.setStatus("submitted");
        submissionMapper.insert(sub);
        return Map.of("submissionId", sub.getId());
    }

    // ============================== 批改（管理员） ==============================

    /**
     * 批改作业：要求该用户存在 submission（否则 DATA_NOT_FOUND）；
     * upsert assignment_grade（graderId=当前管理员）+ submission.status='graded'。
     */
    @Transactional
    public Map<String, Long> grade(Long assignmentId, GradeAssignmentRequest request) {
        // 校验作业存在且未删除
        getActive(assignmentId);

        AssignmentSubmission sub = findSubmission(assignmentId, request.getUserId());
        if (sub == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "该用户尚未提交作业");
        }

        Long graderId = SecurityUtils.currentUserId();
        AssignmentGrade existing = findGradeBySubmission(sub.getId());

        if (existing != null) {
            existing.setAssignmentId(assignmentId);
            existing.setUserId(request.getUserId());
            existing.setGraderId(graderId);
            existing.setScore(request.getScore());
            existing.setFeedback(request.getFeedback());
            gradeMapper.updateById(existing);
        } else {
            AssignmentGrade grade = new AssignmentGrade();
            grade.setSubmissionId(sub.getId());
            grade.setAssignmentId(assignmentId);
            grade.setUserId(request.getUserId());
            grade.setGraderId(graderId);
            grade.setScore(request.getScore());
            grade.setFeedback(request.getFeedback());
            gradeMapper.insert(grade);
        }

        // 提交状态置为 graded
        sub.setStatus("graded");
        submissionMapper.updateById(sub);

        Long gradeId = existing != null ? existing.getId() : lastGradeId(sub.getId());
        return Map.of("gradeId", gradeId);
    }

    // ============================== 工具 ==============================

    private Assignment getActive(Long assignmentId) {
        Assignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null || assignment.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "作业不存在");
        }
        return assignment;
    }

    private AssignmentSubmission findSubmission(Long assignmentId, Long userId) {
        return submissionMapper.selectOne(new LambdaQueryWrapper<AssignmentSubmission>()
                .eq(AssignmentSubmission::getAssignmentId, assignmentId)
                .eq(AssignmentSubmission::getUserId, userId)
                .isNull(AssignmentSubmission::getDeletedAt)
                .last("LIMIT 1"));
    }

    private Map<Long, AssignmentSubmission> loadSubmissions(List<Long> assignmentIds, Long userId) {
        if (assignmentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<AssignmentSubmission> list = submissionMapper.selectList(new LambdaQueryWrapper<AssignmentSubmission>()
                .in(AssignmentSubmission::getAssignmentId, assignmentIds)
                .eq(AssignmentSubmission::getUserId, userId)
                .isNull(AssignmentSubmission::getDeletedAt));
        Map<Long, AssignmentSubmission> map = new HashMap<>();
        for (AssignmentSubmission s : list) {
            map.put(s.getAssignmentId(), s);
        }
        return map;
    }

    private AssignmentGrade findGradeBySubmission(Long submissionId) {
        return gradeMapper.selectOne(new LambdaQueryWrapper<AssignmentGrade>()
                .eq(AssignmentGrade::getSubmissionId, submissionId)
                .isNull(AssignmentGrade::getDeletedAt)
                .last("LIMIT 1"));
    }

    private Map<Long, AssignmentGrade> loadGrades(List<Long> submissionIds) {
        List<AssignmentGrade> list = gradeMapper.selectList(new LambdaQueryWrapper<AssignmentGrade>()
                .in(AssignmentGrade::getSubmissionId, submissionIds)
                .isNull(AssignmentGrade::getDeletedAt));
        Map<Long, AssignmentGrade> map = new HashMap<>();
        for (AssignmentGrade g : list) {
            map.put(g.getSubmissionId(), g);
        }
        return map;
    }

    /** 用户作业状态：无 submission→not_submitted；有 submission 无 grade→submitted；有 grade→graded */
    private String userStatus(AssignmentSubmission sub, AssignmentGrade grade) {
        if (sub == null) {
            return "not_submitted";
        }
        if (grade != null) {
            return "graded";
        }
        return "submitted";
    }

    private AssignmentVO toListItem(Assignment a, AssignmentSubmission sub, AssignmentGrade grade) {
        AssignmentVO vo = new AssignmentVO();
        vo.setId(a.getId());
        vo.setTitle(a.getTitle());
        vo.setDeadline(a.getDeadline());
        vo.setStatus(userStatus(sub, grade));
        if (grade != null) {
            vo.setScore(grade.getScore());
        }
        return vo;
    }

    private AssignmentVO.SubmissionDTO toSubmissionDTO(AssignmentSubmission sub) {
        AssignmentVO.SubmissionDTO dto = new AssignmentVO.SubmissionDTO();
        dto.setId(sub.getId());
        dto.setContent(sub.getContent());
        dto.setAttachments(fromJson(sub.getAttachments()));
        dto.setSubmittedAt(sub.getSubmittedAt());
        dto.setStatus(sub.getStatus());
        return dto;
    }

    private AssignmentVO.GradeDTO toGradeDTO(AssignmentGrade grade) {
        AssignmentVO.GradeDTO dto = new AssignmentVO.GradeDTO();
        dto.setId(grade.getId());
        dto.setScore(grade.getScore());
        dto.setFeedback(grade.getFeedback());
        return dto;
    }

    private AssignmentVO baseVO(Assignment assignment, String status) {
        AssignmentVO vo = new AssignmentVO();
        vo.setId(assignment.getId());
        vo.setTitle(assignment.getTitle());
        vo.setDeadline(assignment.getDeadline());
        vo.setStatus(status);
        return vo;
    }

    /** 取刚插入的 grade id（无自动回填时的兜底） */
    private Long lastGradeId(Long submissionId) {
        AssignmentGrade g = findGradeBySubmission(submissionId);
        return g == null ? null : g.getId();
    }

    private String toJson(List<String> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attachments);
        } catch (JsonProcessingException e) {
            log.warn("序列化附件失败，回落为空数组", e);
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<String> list = objectMapper.readValue(json, LIST_TYPE);
            return list == null ? new ArrayList<>() : list;
        } catch (JsonProcessingException e) {
            log.warn("反序列化附件失败：{}", json, e);
            return new ArrayList<>();
        }
    }
}
