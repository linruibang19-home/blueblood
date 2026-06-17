package com.blueblood.api.modules.task.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.task.dto.MilestoneVO;
import com.blueblood.api.modules.task.dto.ReviewMilestoneRequest;
import com.blueblood.api.modules.task.dto.SubmitMilestoneRequest;
import com.blueblood.api.modules.task.entity.MilestoneReview;
import com.blueblood.api.modules.task.entity.MilestoneSubmission;
import com.blueblood.api.modules.task.entity.TaskMilestone;
import com.blueblood.api.modules.task.entity.TaskOrder;
import com.blueblood.api.modules.task.mapper.MilestoneReviewMapper;
import com.blueblood.api.modules.task.mapper.MilestoneSubmissionMapper;
import com.blueblood.api.modules.task.mapper.TaskMilestoneMapper;
import com.blueblood.api.modules.task.mapper.TaskOrderMapper;
import com.blueblood.api.security.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 里程碑服务：提交、查看审核结果、管理员审核；进度重算。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final TaskMilestoneMapper milestoneMapper;
    private final MilestoneSubmissionMapper submissionMapper;
    private final MilestoneReviewMapper reviewMapper;
    private final TaskOrderMapper orderMapper;
    private final ObjectMapper objectMapper;

    private static final TypeReference<List<String>> LIST_TYPE = new TypeReference<>() {};

    // ============================== 提交 ==============================

    @Transactional
    public Map<String, Long> submit(Long milestoneId, SubmitMilestoneRequest req) {
        TaskMilestone m = getMilestone(milestoneId);
        TaskOrder order = ownOrder(m.getOrderId());

        String st = m.getStatus();
        if ("SUBMITTED".equalsIgnoreCase(st)) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "已提交，待审核");
        }
        if ("APPROVED".equalsIgnoreCase(st)) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "该里程碑已通过，不可重复提交");
        }

        // upsert 提交记录(UNIQUE milestone_id)
        MilestoneSubmission sub = submissionMapper.selectOne(new LambdaQueryWrapper<MilestoneSubmission>()
                .eq(MilestoneSubmission::getMilestoneId, milestoneId)
                .isNull(MilestoneSubmission::getDeletedAt)
                .last("LIMIT 1"));
        String attachmentsJson = toJson(req.getAttachments());
        LocalDateTime now = LocalDateTime.now();
        if (sub == null) {
            sub = new MilestoneSubmission();
            sub.setMilestoneId(milestoneId);
            sub.setOrderId(m.getOrderId());
            sub.setUserId(order.getUserId());
            sub.setGithubUrl(req.getGithubUrl() == null ? "" : req.getGithubUrl());
            sub.setDescription(req.getDescription() == null ? "" : req.getDescription());
            sub.setAttachments(attachmentsJson);
            sub.setSubmittedAt(now);
            submissionMapper.insert(sub);
        } else {
            sub.setGithubUrl(req.getGithubUrl() == null ? "" : req.getGithubUrl());
            sub.setDescription(req.getDescription() == null ? "" : req.getDescription());
            sub.setAttachments(attachmentsJson);
            sub.setSubmittedAt(now);
            submissionMapper.updateById(sub);
        }

        // 里程碑 → SUBMITTED；重置审核为 PENDING
        m.setStatus("SUBMITTED");
        milestoneMapper.updateById(m);
        upsertReviewPending(milestoneId, sub.getId());

        return Map.of("submissionId", sub.getId());
    }

    // ============================== 查看审核结果 ==============================

    public MilestoneVO result(Long milestoneId) {
        TaskMilestone m = getMilestone(milestoneId);
        ownOrder(m.getOrderId());
        return buildVO(m);
    }

    // ============================== 管理员审核 ==============================

    @Transactional
    public Map<String, Long> review(Long milestoneId, ReviewMilestoneRequest req) {
        TaskMilestone m = getMilestone(milestoneId);
        if (!"SUBMITTED".equalsIgnoreCase(m.getStatus())) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, "该里程碑不在待审核状态");
        }
        Long reviewerId = SecurityUtils.currentUserId();
        String result = req.getResult().toUpperCase();

        MilestoneReview review = reviewMapper.selectOne(new LambdaQueryWrapper<MilestoneReview>()
                .eq(MilestoneReview::getMilestoneId, milestoneId)
                .isNull(MilestoneReview::getDeletedAt)
                .last("LIMIT 1"));
        if (review == null) {
            review = new MilestoneReview();
            review.setMilestoneId(milestoneId);
            review.setReviewerId(reviewerId);
            review.setResult(result);
            review.setFeedback(req.getFeedback() == null ? "" : req.getFeedback());
            review.setReviewedAt(LocalDateTime.now());
            reviewMapper.insert(review);
        } else {
            review.setReviewerId(reviewerId);
            review.setResult(result);
            review.setFeedback(req.getFeedback() == null ? "" : req.getFeedback());
            review.setReviewedAt(LocalDateTime.now());
            reviewMapper.updateById(review);
        }

        // 里程碑状态流转
        m.setStatus(result);
        milestoneMapper.updateById(m);

        // 重算订单进度 + 是否全部通过
        recomputeOrderProgress(m.getOrderId());

        return Map.of("reviewId", review.getId());
    }

    // ============================== 进度重算 ==============================

    /** 重算订单整体进度；全部里程碑通过则订单进入待验收。 */
    public void recomputeOrderProgress(Long orderId) {
        List<TaskMilestone> all = milestoneMapper.selectList(new LambdaQueryWrapper<TaskMilestone>()
                .eq(TaskMilestone::getOrderId, orderId)
                .isNull(TaskMilestone::getDeletedAt));
        if (all.isEmpty()) {
            return;
        }
        long approved = all.stream().filter(m -> "APPROVED".equalsIgnoreCase(m.getStatus())).count();
        int progress = (int) Math.round(approved * 100.0 / all.size());

        TaskOrder patch = new TaskOrder();
        patch.setId(orderId);
        patch.setProgress(progress);
        if (approved == all.size()) {
            patch.setStatus("wait_acceptance");
        }
        orderMapper.updateById(patch);
    }

    // ============================== VO 构建 ==============================

    public MilestoneVO buildVO(TaskMilestone m) {
        MilestoneVO vo = new MilestoneVO();
        vo.setId(m.getId());
        vo.setOrderId(m.getOrderId());
        vo.setTaskId(m.getTaskId());
        vo.setTitle(m.getTitle());
        vo.setDescription(m.getDescription());
        vo.setDueDate(m.getDueDate());
        vo.setMilestoneOrder(m.getMilestoneOrder());
        vo.setStatus(m.getStatus());

        MilestoneSubmission sub = submissionMapper.selectOne(new LambdaQueryWrapper<MilestoneSubmission>()
                .eq(MilestoneSubmission::getMilestoneId, m.getId())
                .isNull(MilestoneSubmission::getDeletedAt)
                .last("LIMIT 1"));
        if (sub != null) {
            MilestoneVO.SubmissionSummary ss = new MilestoneVO.SubmissionSummary();
            ss.setId(sub.getId());
            ss.setGithubUrl(sub.getGithubUrl());
            ss.setDescription(sub.getDescription());
            ss.setSubmittedAt(sub.getSubmittedAt());
            vo.setSubmission(ss);
        }

        MilestoneReview review = reviewMapper.selectOne(new LambdaQueryWrapper<MilestoneReview>()
                .eq(MilestoneReview::getMilestoneId, m.getId())
                .isNull(MilestoneReview::getDeletedAt)
                .last("LIMIT 1"));
        if (review != null) {
            MilestoneVO.ReviewSummary rs = new MilestoneVO.ReviewSummary();
            rs.setResult(review.getResult());
            rs.setFeedback(review.getFeedback());
            rs.setReviewedAt(review.getReviewedAt());
            vo.setReview(rs);
        }
        return vo;
    }

    // ============================== 工具 ==============================

    private TaskMilestone getMilestone(Long id) {
        TaskMilestone m = milestoneMapper.selectById(id);
        if (m == null || m.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "里程碑不存在");
        }
        return m;
    }

    /** 校验订单属于当前用户，返回订单。 */
    private TaskOrder ownOrder(Long orderId) {
        TaskOrder order = orderMapper.selectById(orderId);
        if (order == null || order.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "订单不存在");
        }
        if (!order.getUserId().equals(SecurityUtils.currentUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该订单");
        }
        return order;
    }

    private void upsertReviewPending(Long milestoneId, Long submissionId) {
        MilestoneReview review = reviewMapper.selectOne(new LambdaQueryWrapper<MilestoneReview>()
                .eq(MilestoneReview::getMilestoneId, milestoneId)
                .isNull(MilestoneReview::getDeletedAt)
                .last("LIMIT 1"));
        if (review == null) {
            review = new MilestoneReview();
            review.setMilestoneId(milestoneId);
            review.setSubmissionId(submissionId);
            review.setResult("PENDING");
            reviewMapper.insert(review);
        } else {
            review.setSubmissionId(submissionId);
            review.setResult("PENDING");
            review.setFeedback("");
            review.setReviewedAt(null);
            reviewMapper.updateById(review);
        }
    }

    private String toJson(List<String> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(attachments);
        } catch (JsonProcessingException e) {
            log.warn("序列化附件失败", e);
            return "[]";
        }
    }

    @SuppressWarnings("unused")
    private List<String> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<String> list = objectMapper.readValue(json, LIST_TYPE);
            return list == null ? List.of() : list;
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
