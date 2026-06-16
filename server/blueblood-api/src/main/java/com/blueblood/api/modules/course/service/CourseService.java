package com.blueblood.api.modules.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.course.dto.ChapterVO;
import com.blueblood.api.modules.course.dto.CourseListItemVO;
import com.blueblood.api.modules.course.dto.CourseVO;
import com.blueblood.api.modules.course.dto.ProgressVO;
import com.blueblood.api.modules.course.dto.UpdateProgressRequest;
import com.blueblood.api.modules.course.entity.Course;
import com.blueblood.api.modules.course.entity.CourseChapter;
import com.blueblood.api.modules.course.entity.CourseProgress;
import com.blueblood.api.modules.course.mapper.CourseChapterMapper;
import com.blueblood.api.modules.course.mapper.CourseMapper;
import com.blueblood.api.modules.course.mapper.CourseProgressMapper;
import com.blueblood.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课程服务：列表、详情、章节、进度推进、当前章节。
 *
 * <p>进度推进模型（线性章节，无需新表）：</p>
 * <ul>
 *   <li>章节按 chapter_order 升序视为学习顺序。</li>
 *   <li>完成第 k 章（chapter_order=k，1 基）→ completed_chapters = max(原值, k)。</li>
 *   <li>progress% = round(completed_chapters / total_chapters * 100)。</li>
 *   <li>status：completed_chapters==0 → not_started；==total_chapters → completed；否则 in_progress。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper courseMapper;
    private final CourseChapterMapper courseChapterMapper;
    private final CourseProgressMapper courseProgressMapper;

    // ============================== 列表 ==============================

    /**
     * 课程分页：每项含课程基本信息 + 当前用户 progress/status。
     */
    public PageResult<CourseListItemVO> page(Integer page, Integer pageSize) {
        Page<Course> p = new Page<>(page == null || page < 1 ? 1 : page,
                pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100));
        Page<Course> result = courseMapper.selectPage(p, new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, "PUBLISHED")
                .isNull(Course::getDeletedAt)
                .orderByDesc(Course::getCreatedAt));

        List<Course> courses = result.getRecords();
        Long userId = SecurityUtils.currentUserId();

        List<CourseListItemVO> list = (courses == null ? Collections.<Course>emptyList() : courses)
                .stream().map(c -> {
                    CourseListItemVO vo = new CourseListItemVO();
                    vo.setId(c.getId());
                    vo.setTitle(c.getTitle());
                    vo.setSubtitle(c.getSubtitle());
                    vo.setCoverImage(c.getCoverImage());
                    vo.setInstructor(c.getInstructor());
                    vo.setTotalChapters(c.getTotalChapters());
                    vo.setRewardPoints(c.getRewardPoints());
                    vo.setStudents(c.getStudents());
                    vo.setRating(c.getRating());

                    CourseProgress prog = getProgress(c.getId(), userId);
                    vo.setProgress(prog != null && prog.getProgress() != null ? prog.getProgress() : 0);
                    vo.setStatus(prog != null && prog.getStatus() != null ? prog.getStatus() : "not_started");
                    return vo;
                }).collect(Collectors.toList());

        return new PageResult<>(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    // ============================== 详情 ==============================

    /**
     * 课程详情：课程全部字段 + 当前用户进度 + chapters 列表（每章带 completed）。
     */
    public CourseVO detail(Long id) {
        Course course = getCourseOrThrow(id);
        Long userId = SecurityUtils.currentUserId();

        CourseProgress prog = getProgress(id, userId);
        int completed = prog != null && prog.getCompletedChapters() != null ? prog.getCompletedChapters() : 0;

        CourseVO vo = new CourseVO();
        vo.setId(course.getId());
        vo.setTitle(course.getTitle());
        vo.setSubtitle(course.getSubtitle());
        vo.setCoverImage(course.getCoverImage());
        vo.setInstructor(course.getInstructor());
        vo.setInstructorAvatar(course.getInstructorAvatar());
        vo.setTotalChapters(course.getTotalChapters());
        vo.setRewardPoints(course.getRewardPoints());
        vo.setStudents(course.getStudents());
        vo.setRating(course.getRating());
        vo.setStatus(course.getStatus());

        vo.setProgress(prog != null && prog.getProgress() != null ? prog.getProgress() : 0);
        vo.setCompletedChapters(completed);
        vo.setStatusForUser(prog != null && prog.getStatus() != null ? prog.getStatus() : "not_started");
        vo.setLastStudyAt(prog != null ? prog.getLastStudyAt() : null);

        vo.setChapters(listChapterVOs(id, completed));
        return vo;
    }

    // ============================== 章节列表 ==============================

    /**
     * 章节列表（chapter_order asc，每项带 completed）。
     */
    public List<ChapterVO> listChapters(Long courseId) {
        getCourseOrThrow(courseId);
        Long userId = SecurityUtils.currentUserId();
        int completed = getCompletedChapters(courseId, userId);
        return listChapterVOs(courseId, completed);
    }

    // ============================== 进度推进 ==============================

    /**
     * 更新进度：查 chapter 得 chapter_order，按线性模型 upsert course_progress，
     * 更新 lastStudyAt=now；返回新的 {completedChapters,progress,status}。
     *
     * <p>completed 为 false 时不回退进度（线性模型仅前进），但仍刷新 lastStudyAt。</p>
     */
    @Transactional
    public ProgressVO updateProgress(Long courseId, UpdateProgressRequest req) {
        Course course = getCourseOrThrow(courseId);

        // 校验章节属于该课程
        CourseChapter chapter = courseChapterMapper.selectById(req.getChapterId());
        if (chapter == null || chapter.getDeletedAt() != null
                || !courseId.equals(chapter.getCourseId())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "章节不存在");
        }

        Long userId = SecurityUtils.currentUserId();
        int totalChapters = course.getTotalChapters() == null ? 0 : course.getTotalChapters();

        // upsert 进度记录
        CourseProgress prog = getProgress(courseId, userId);
        if (prog == null) {
            prog = new CourseProgress();
            prog.setCourseId(courseId);
            prog.setUserId(userId);
            prog.setCompletedChapters(0);
            prog.setProgress(0);
            prog.setStatus("not_started");
            courseProgressMapper.insert(prog);
        }

        // 推进：completed_chapters = max(原值, chapter_order)
        if (Boolean.TRUE.equals(req.getCompleted())) {
            int order = chapter.getChapterOrder() == null ? 0 : chapter.getChapterOrder();
            if (order > prog.getCompletedChapters()) {
                prog.setCompletedChapters(order);
            }
        }

        // 重算 progress / status
        recompute(prog, totalChapters);
        prog.setLastStudyAt(LocalDateTime.now());
        courseProgressMapper.updateById(prog);

        ProgressVO out = new ProgressVO();
        out.setCompletedChapters(prog.getCompletedChapters());
        out.setProgress(prog.getProgress());
        out.setStatus(prog.getStatus());
        return out;
    }

    // ============================== 当前章节 ==============================

    /**
     * 当前学习章节：第一个 completed=false 的章节（chapter_order==completedChapters+1）。
     * 全部完成则返回最后一个章节。
     */
    public ChapterVO currentChapter(Long courseId) {
        getCourseOrThrow(courseId);
        Long userId = SecurityUtils.currentUserId();
        int completed = getCompletedChapters(courseId, userId);

        List<CourseChapter> chapters = courseChapterMapper.selectList(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId)
                .isNull(CourseChapter::getDeletedAt)
                .orderByAsc(CourseChapter::getChapterOrder));
        if (chapters == null || chapters.isEmpty()) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "该课程暂无章节");
        }

        // 第一个未完成的章节
        for (CourseChapter ch : chapters) {
            int order = ch.getChapterOrder() == null ? 0 : ch.getChapterOrder();
            if (order > completed) {
                return toChapterVO(ch, completed);
            }
        }
        // 全部完成 → 返回最后一个章节
        CourseChapter last = chapters.get(chapters.size() - 1);
        return toChapterVO(last, completed);
    }

    // ============================== 内部 ==============================

    private Course getCourseOrThrow(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null || course.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "课程不存在");
        }
        return course;
    }

    /**
     * 查询当前用户对某课程的进度记录（仅未软删）。
     */
    private CourseProgress getProgress(Long courseId, Long userId) {
        return courseProgressMapper.selectOne(new LambdaQueryWrapper<CourseProgress>()
                .eq(CourseProgress::getCourseId, courseId)
                .eq(CourseProgress::getUserId, userId)
                .isNull(CourseProgress::getDeletedAt)
                .last("LIMIT 1"));
    }

    private int getCompletedChapters(Long courseId, Long userId) {
        CourseProgress prog = getProgress(courseId, userId);
        return prog != null && prog.getCompletedChapters() != null ? prog.getCompletedChapters() : 0;
    }

    /**
     * 构造章节 VO 列表（chapter_order asc，带 completed 标记）。
     */
    private List<ChapterVO> listChapterVOs(Long courseId, int completedChapters) {
        List<CourseChapter> chapters = courseChapterMapper.selectList(new LambdaQueryWrapper<CourseChapter>()
                .eq(CourseChapter::getCourseId, courseId)
                .isNull(CourseChapter::getDeletedAt)
                .orderByAsc(CourseChapter::getChapterOrder));
        if (chapters == null || chapters.isEmpty()) {
            return Collections.emptyList();
        }
        return chapters.stream().map(ch -> toChapterVO(ch, completedChapters)).collect(Collectors.toList());
    }

    private ChapterVO toChapterVO(CourseChapter ch, int completedChapters) {
        ChapterVO vo = new ChapterVO();
        vo.setId(ch.getId());
        vo.setCourseId(ch.getCourseId());
        vo.setTitle(ch.getTitle());
        vo.setDuration(ch.getDuration());
        vo.setVideoUrl(ch.getVideoUrl());
        vo.setChapterOrder(ch.getChapterOrder());
        int order = ch.getChapterOrder() == null ? 0 : ch.getChapterOrder();
        vo.setCompleted(order <= completedChapters);
        return vo;
    }

    /**
     * 按 completed_chapters 与 total_chapters 重算 progress% 与 status。
     */
    private void recompute(CourseProgress prog, int totalChapters) {
        int completed = prog.getCompletedChapters() == null ? 0 : prog.getCompletedChapters();
        int progress;
        String status;
        if (totalChapters <= 0) {
            progress = 0;
            status = "not_started";
        } else {
            if (completed >= totalChapters) {
                progress = 100;
                status = "completed";
            } else if (completed <= 0) {
                progress = 0;
                status = "not_started";
            } else {
                progress = (int) Math.round(completed * 100.0 / totalChapters);
                status = "in_progress";
            }
        }
        prog.setProgress(progress);
        prog.setStatus(status);
    }
}
