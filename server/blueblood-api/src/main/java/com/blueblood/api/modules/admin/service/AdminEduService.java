package com.blueblood.api.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageQuery;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.admin.dto.*;
import com.blueblood.api.modules.assignment.entity.Assignment;
import com.blueblood.api.modules.assignment.entity.AssignmentGrade;
import com.blueblood.api.modules.assignment.entity.AssignmentSubmission;
import com.blueblood.api.modules.assignment.mapper.AssignmentGradeMapper;
import com.blueblood.api.modules.assignment.mapper.AssignmentMapper;
import com.blueblood.api.modules.assignment.mapper.AssignmentSubmissionMapper;
import com.blueblood.api.modules.course.entity.Course;
import com.blueblood.api.modules.course.entity.CourseChapter;
import com.blueblood.api.modules.course.mapper.CourseChapterMapper;
import com.blueblood.api.modules.course.mapper.CourseMapper;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.UserMapper;
import com.blueblood.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 后台教育内容管理：课程 / 章节 / 作业 / 提交记录 / 批改。
 * 列表 VO 携带 courseTitle / studentNickname / score 等冗余字段。
 * 删除均为软删(set deletedAt=now)。
 */
@Service
@RequiredArgsConstructor
public class AdminEduService {

    private final CourseMapper courseMapper;
    private final CourseChapterMapper chapterMapper;
    private final AssignmentMapper assignmentMapper;
    private final AssignmentSubmissionMapper submissionMapper;
    private final AssignmentGradeMapper gradeMapper;
    private final UserMapper userMapper;

    private static final Set<String> COURSE_STATUS = Set.of("PUBLISHED", "DRAFT", "OFFLINE");

    // ==================== 课程 ====================

    public PageResult<AdminCourseVO> pageCourse(AdminCourseQuery query) {
        Page<Course> page = query.toPage();
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .isNull(Course::getDeletedAt)
                .orderByDesc(Course::getCreatedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(Course::getTitle, kw).or().like(Course::getSubtitle, kw));
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Course::getStatus, query.getStatus().toUpperCase());
        }
        Page<Course> result = courseMapper.selectPage(page, wrapper);
        return PageResult.of(result.convert(this::toCourseVO));
    }

    public AdminCourseVO getCourse(Long id) {
        return toCourseVO(getCourseEntity(id));
    }

    @Transactional
    public Long createCourse(CourseRequest req) {
        Course c = new Course();
        applyCourseFields(c, req);
        if (!StringUtils.hasText(c.getStatus())) {
            c.setStatus("DRAFT");
        }
        if (c.getStudents() == null) {
            c.setStudents(0);
        }
        if (c.getRating() == null) {
            c.setRating(BigDecimal.ZERO);
        }
        courseMapper.insert(c);
        return c.getId();
    }

    @Transactional
    public void updateCourse(Long id, CourseRequest req) {
        Course exists = getCourseEntity(id);
        applyCourseFields(exists, req);
        courseMapper.updateById(exists);
    }

    @Transactional
    public void updateCourseStatus(Long id, String status) {
        if (!StringUtils.hasText(status) || !COURSE_STATUS.contains(status.toUpperCase())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "非法课程状态");
        }
        Course patch = new Course();
        patch.setId(id);
        patch.setStatus(status.toUpperCase());
        courseMapper.updateById(patch);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course c = getCourseEntity(id);
        Course patch = new Course();
        patch.setId(c.getId());
        patch.setDeletedAt(LocalDateTime.now());
        courseMapper.updateById(patch);
    }

    // ==================== 章节 ====================

    public List<AdminChapterVO> listChapters(Long courseId) {
        getCourseEntity(courseId);
        List<CourseChapter> list = chapterMapper.selectList(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId)
                .isNull(CourseChapter::getDeletedAt)
                .orderByAsc(CourseChapter::getChapterOrder)
                .orderByAsc(CourseChapter::getId));
        String courseTitle = loadCourseTitles(Collections.singletonList(courseId)).get(courseId);
        return list.stream().map(ch -> toChapterVO(ch, courseTitle)).toList();
    }

    @Transactional
    public Long createChapter(ChapterRequest req) {
        getCourseEntity(req.getCourseId());
        CourseChapter ch = new CourseChapter();
        applyChapterFields(ch, req);
        if (ch.getChapterOrder() == null) {
            ch.setChapterOrder(nextChapterOrder(req.getCourseId()));
        }
        chapterMapper.insert(ch);
        return ch.getId();
    }

    @Transactional
    public void updateChapter(Long id, ChapterRequest req) {
        CourseChapter exists = getChapterEntity(id);
        if (req.getCourseId() != null) {
            getCourseEntity(req.getCourseId());
        }
        applyChapterFields(exists, req);
        chapterMapper.updateById(exists);
    }

    @Transactional
    public void deleteChapter(Long id) {
        CourseChapter ch = getChapterEntity(id);
        CourseChapter patch = new CourseChapter();
        patch.setId(ch.getId());
        patch.setDeletedAt(LocalDateTime.now());
        chapterMapper.updateById(patch);
    }

    private Integer nextChapterOrder(Long courseId) {
        List<CourseChapter> list = chapterMapper.selectList(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId)
                .isNull(CourseChapter::getDeletedAt));
        return list.size() + 1;
    }

    // ==================== 作业 ====================

    public PageResult<AdminAssignmentVO> pageAssignment(AdminAssignmentQuery query) {
        Page<Assignment> page = query.toPage();
        LambdaQueryWrapper<Assignment> wrapper = new LambdaQueryWrapper<Assignment>()
                .isNull(Assignment::getDeletedAt)
                .orderByDesc(Assignment::getDeadline)
                .orderByDesc(Assignment::getId);
        if (query.getCourseId() != null) {
            wrapper.eq(Assignment::getCourseId, query.getCourseId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(Assignment::getTitle, kw).or().like(Assignment::getDescription, kw));
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Assignment::getStatus, query.getStatus());
        }
        Page<Assignment> result = assignmentMapper.selectPage(page, wrapper);
        Map<Long, String> courseMap = loadCourseTitles(
                result.getRecords().stream().map(Assignment::getCourseId).filter(Objects::nonNull).distinct().toList());
        return PageResult.of(result.convert(a -> toAssignmentVO(a, courseMap.get(a.getCourseId()))));
    }

    public AdminAssignmentVO getAssignment(Long id) {
        Assignment a = getAssignmentEntity(id);
        String courseTitle = loadCourseTitles(Collections.singletonList(a.getCourseId())).get(a.getCourseId());
        return toAssignmentVO(a, courseTitle);
    }

    @Transactional
    public Long createAssignment(AssignmentRequest req) {
        getCourseEntity(req.getCourseId());
        Assignment a = new Assignment();
        applyAssignmentFields(a, req);
        if (!StringUtils.hasText(a.getStatus())) {
            a.setStatus("not_submitted");
        }
        assignmentMapper.insert(a);
        return a.getId();
    }

    @Transactional
    public void updateAssignment(Long id, AssignmentRequest req) {
        Assignment exists = getAssignmentEntity(id);
        if (req.getCourseId() != null) {
            getCourseEntity(req.getCourseId());
        }
        applyAssignmentFields(exists, req);
        assignmentMapper.updateById(exists);
    }

    @Transactional
    public void deleteAssignment(Long id) {
        Assignment a = getAssignmentEntity(id);
        Assignment patch = new Assignment();
        patch.setId(a.getId());
        patch.setDeletedAt(LocalDateTime.now());
        assignmentMapper.updateById(patch);
    }

    // ==================== 提交 ====================

    public PageResult<AdminSubmissionVO> pageSubmission(Long assignmentId, String status, PageQuery query) {
        Page<AssignmentSubmission> page = query.toPage();
        LambdaQueryWrapper<AssignmentSubmission> wrapper = new LambdaQueryWrapper<AssignmentSubmission>()
                .isNull(AssignmentSubmission::getDeletedAt)
                .orderByDesc(AssignmentSubmission::getSubmittedAt);
        if (assignmentId != null) {
            wrapper.eq(AssignmentSubmission::getAssignmentId, assignmentId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(AssignmentSubmission::getStatus, status);
        }
        Page<AssignmentSubmission> result = submissionMapper.selectPage(page, wrapper);
        List<AssignmentSubmission> records = result.getRecords();

        Map<Long, String> studentMap = loadNicknames(
                records.stream().map(AssignmentSubmission::getUserId).filter(Objects::nonNull).distinct().toList());
        Map<Long, String> assignTitleMap = loadAssignmentTitles(
                records.stream().map(AssignmentSubmission::getAssignmentId).filter(Objects::nonNull).distinct().toList());
        Map<Long, AssignmentGrade> gradeMap = loadGrades(
                records.stream().map(AssignmentSubmission::getId).distinct().toList());

        return PageResult.of(result.convert(s -> toSubmissionVO(s,
                studentMap.get(s.getUserId()),
                assignTitleMap.get(s.getAssignmentId()),
                gradeMap.get(s.getId()))));
    }

    // ==================== 批改 ====================

    /**
     * 按 submissionId 批改：upsert assignment_grade(graderId=当前管理员) +
     * submission.status='graded'。
     */
    @Transactional
    public Long gradeSubmission(Long submissionId, GradeSubmissionRequest req) {
        AssignmentSubmission sub = getSubmissionEntity(submissionId);
        Long graderId = SecurityUtils.currentUserId();

        AssignmentGrade existing = gradeMapper.selectOne(new LambdaQueryWrapper<AssignmentGrade>()
                .eq(AssignmentGrade::getSubmissionId, submissionId)
                .isNull(AssignmentGrade::getDeletedAt)
                .last("LIMIT 1"));

        if (existing != null) {
            existing.setAssignmentId(sub.getAssignmentId());
            existing.setUserId(sub.getUserId());
            existing.setGraderId(graderId);
            existing.setScore(req.getScore());
            existing.setFeedback(req.getFeedback());
            gradeMapper.updateById(existing);
        } else {
            AssignmentGrade grade = new AssignmentGrade();
            grade.setSubmissionId(submissionId);
            grade.setAssignmentId(sub.getAssignmentId());
            grade.setUserId(sub.getUserId());
            grade.setGraderId(graderId);
            grade.setScore(req.getScore());
            grade.setFeedback(req.getFeedback());
            gradeMapper.insert(grade);
        }

        sub.setStatus("graded");
        submissionMapper.updateById(sub);
        return existing != null ? existing.getId() : submissionId;
    }

    // ==================== 实体取数 ====================

    private Course getCourseEntity(Long id) {
        Course c = courseMapper.selectById(id);
        if (c == null || c.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "课程不存在");
        }
        return c;
    }

    private CourseChapter getChapterEntity(Long id) {
        CourseChapter ch = chapterMapper.selectById(id);
        if (ch == null || ch.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "章节不存在");
        }
        return ch;
    }

    private Assignment getAssignmentEntity(Long id) {
        Assignment a = assignmentMapper.selectById(id);
        if (a == null || a.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "作业不存在");
        }
        return a;
    }

    private AssignmentSubmission getSubmissionEntity(Long id) {
        AssignmentSubmission s = submissionMapper.selectById(id);
        if (s == null || s.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "提交记录不存在");
        }
        return s;
    }

    // ==================== 字段拷贝 ====================

    private void applyCourseFields(Course c, CourseRequest req) {
        c.setTitle(req.getTitle());
        c.setSubtitle(req.getSubtitle());
        c.setCoverImage(req.getCoverImage());
        c.setInstructor(req.getInstructor());
        c.setInstructorAvatar(req.getInstructorAvatar());
        if (req.getTotalChapters() != null) {
            c.setTotalChapters(req.getTotalChapters());
        }
        if (req.getRewardPoints() != null) {
            c.setRewardPoints(req.getRewardPoints());
        }
        if (req.getRating() != null) {
            c.setRating(req.getRating());
        }
        if (StringUtils.hasText(req.getStatus())) {
            String s = req.getStatus().toUpperCase();
            if (!COURSE_STATUS.contains(s)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "非法课程状态");
            }
            c.setStatus(s);
        }
    }

    private void applyChapterFields(CourseChapter ch, ChapterRequest req) {
        if (req.getCourseId() != null) {
            ch.setCourseId(req.getCourseId());
        }
        ch.setTitle(req.getTitle());
        ch.setDuration(req.getDuration());
        ch.setVideoUrl(req.getVideoUrl());
        if (req.getChapterOrder() != null) {
            ch.setChapterOrder(req.getChapterOrder());
        }
    }

    private void applyAssignmentFields(Assignment a, AssignmentRequest req) {
        if (req.getCourseId() != null) {
            a.setCourseId(req.getCourseId());
        }
        a.setChapterId(req.getChapterId());
        a.setTitle(req.getTitle());
        a.setDescription(req.getDescription());
        a.setDeadline(req.getDeadline());
        a.setAnswer(req.getAnswer());
        if (StringUtils.hasText(req.getStatus())) {
            a.setStatus(req.getStatus());
        }
    }

    // ==================== VO 映射 ====================

    private AdminCourseVO toCourseVO(Course c) {
        AdminCourseVO vo = new AdminCourseVO();
        vo.setId(c.getId());
        vo.setTitle(c.getTitle());
        vo.setSubtitle(c.getSubtitle());
        vo.setCoverImage(c.getCoverImage());
        vo.setInstructor(c.getInstructor());
        vo.setInstructorAvatar(c.getInstructorAvatar());
        vo.setTotalChapters(c.getTotalChapters());
        vo.setRewardPoints(c.getRewardPoints());
        vo.setStudents(c.getStudents());
        vo.setRating(c.getRating());
        vo.setStatus(c.getStatus());
        vo.setCreatedAt(c.getCreatedAt());
        return vo;
    }

    private AdminChapterVO toChapterVO(CourseChapter ch, String courseTitle) {
        AdminChapterVO vo = new AdminChapterVO();
        vo.setId(ch.getId());
        vo.setCourseId(ch.getCourseId());
        vo.setCourseTitle(courseTitle);
        vo.setTitle(ch.getTitle());
        vo.setDuration(ch.getDuration());
        vo.setVideoUrl(ch.getVideoUrl());
        vo.setChapterOrder(ch.getChapterOrder());
        vo.setCreatedAt(ch.getCreatedAt());
        return vo;
    }

    private AdminAssignmentVO toAssignmentVO(Assignment a, String courseTitle) {
        AdminAssignmentVO vo = new AdminAssignmentVO();
        vo.setId(a.getId());
        vo.setCourseId(a.getCourseId());
        vo.setCourseTitle(courseTitle);
        vo.setChapterId(a.getChapterId());
        vo.setTitle(a.getTitle());
        vo.setDescription(a.getDescription());
        vo.setDeadline(a.getDeadline());
        vo.setAnswer(a.getAnswer());
        vo.setStatus(a.getStatus());
        vo.setCreatedAt(a.getCreatedAt());
        return vo;
    }

    private AdminSubmissionVO toSubmissionVO(AssignmentSubmission s, String studentNickname,
                                             String assignmentTitle, AssignmentGrade grade) {
        AdminSubmissionVO vo = new AdminSubmissionVO();
        vo.setId(s.getId());
        vo.setAssignmentId(s.getAssignmentId());
        vo.setAssignmentTitle(assignmentTitle);
        vo.setUserId(s.getUserId());
        vo.setStudentNickname(studentNickname);
        vo.setContent(s.getContent());
        vo.setAttachments(s.getAttachments());
        vo.setSubmittedAt(s.getSubmittedAt());
        vo.setStatus(s.getStatus());
        if (grade != null) {
            vo.setScore(grade.getScore());
            vo.setFeedback(grade.getFeedback());
        }
        return vo;
    }

    // ==================== 批量关联 ====================

    private Map<Long, String> loadNicknames(List<Long> userIds) {
        Map<Long, String> map = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return map;
        }
        List<User> users = userMapper.selectBatchIds(userIds);
        for (User u : users) {
            if (u.getDeletedAt() == null) {
                map.put(u.getId(), u.getNickname());
            }
        }
        return map;
    }

    private Map<Long, String> loadCourseTitles(List<Long> courseIds) {
        Map<Long, String> map = new HashMap<>();
        if (courseIds == null || courseIds.isEmpty()) {
            return map;
        }
        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        for (Course c : courses) {
            if (c.getDeletedAt() == null) {
                map.put(c.getId(), c.getTitle());
            }
        }
        return map;
    }

    private Map<Long, String> loadAssignmentTitles(List<Long> assignmentIds) {
        Map<Long, String> map = new HashMap<>();
        if (assignmentIds == null || assignmentIds.isEmpty()) {
            return map;
        }
        List<Assignment> assignments = assignmentMapper.selectBatchIds(assignmentIds);
        for (Assignment a : assignments) {
            if (a.getDeletedAt() == null) {
                map.put(a.getId(), a.getTitle());
            }
        }
        return map;
    }

    private Map<Long, AssignmentGrade> loadGrades(List<Long> submissionIds) {
        Map<Long, AssignmentGrade> map = new HashMap<>();
        if (submissionIds == null || submissionIds.isEmpty()) {
            return map;
        }
        List<AssignmentGrade> grades = gradeMapper.selectList(new LambdaQueryWrapper<AssignmentGrade>()
                .in(AssignmentGrade::getSubmissionId, submissionIds)
                .isNull(AssignmentGrade::getDeletedAt));
        for (AssignmentGrade g : grades) {
            map.put(g.getSubmissionId(), g);
        }
        return map;
    }
}
