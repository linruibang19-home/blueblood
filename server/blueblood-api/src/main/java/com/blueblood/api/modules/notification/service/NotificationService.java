package com.blueblood.api.modules.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.notification.dto.CreateNotificationRequest;
import com.blueblood.api.modules.notification.dto.NotificationVO;
import com.blueblood.api.modules.notification.entity.Notification;
import com.blueblood.api.modules.notification.entity.NotificationRead;
import com.blueblood.api.modules.notification.mapper.NotificationMapper;
import com.blueblood.api.modules.notification.mapper.NotificationReadMapper;
import com.blueblood.api.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通知服务：我的通知分页、未读数、详情（自动已读）、标记已读、批量已读、管理员创建。
 * 已读判定：notification_read 表中存在 (notification_id, user_id) 记录即视为已读
 * （表上有 UNIQUE notification_id + user_id 约束）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationReadMapper notificationReadMapper;

    // ============================== 我的通知分页 ==============================

    /**
     * 当前用户的通知分页（可按 type 筛选，createdAt desc），每项含 read 布尔。
     */
    public PageResult<NotificationVO> pageMy(Integer page, Integer pageSize, String type) {
        Long userId = SecurityUtils.currentUserId();

        Page<Notification> p = new Page<>(
                page == null || page < 1 ? 1 : page,
                pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 100));

        Page<Notification> result = notificationMapper.selectPage(p, new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(type != null && !type.isBlank(), Notification::getType, type)
                .isNull(Notification::getDeletedAt)
                .orderByDesc(Notification::getCreatedAt));

        List<Notification> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), result.getTotal(),
                    result.getCurrent(), result.getSize());
        }

        // 批量查当前用户在分页内已读的通知ID集合，用于填充 read 布尔
        Set<Long> notificationIds = records.stream().map(Notification::getId).collect(Collectors.toSet());
        Set<Long> readIds = loadReadIds(userId, notificationIds);

        List<NotificationVO> list = records.stream().map(n -> toVO(n, readIds.contains(n.getId()))).collect(Collectors.toList());
        return new PageResult<>(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    // ============================== 未读数 ==============================

    /**
     * 当前用户未读通知数 = 属于该用户的通知中，notification_read 无记录的数量。
     */
    public long unreadCount() {
        Long userId = SecurityUtils.currentUserId();
        // 用户全部通知ID
        List<Notification> mine = notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .select(Notification::getId)
                .eq(Notification::getUserId, userId)
                .isNull(Notification::getDeletedAt));
        if (mine.isEmpty()) {
            return 0;
        }
        Set<Long> ids = mine.stream().map(Notification::getId).collect(Collectors.toSet());

        // 这些通知中已被当前用户读过的数量
        Long readCnt = notificationReadMapper.selectCount(new LambdaQueryWrapper<NotificationRead>()
                .eq(NotificationRead::getUserId, userId)
                .in(NotificationRead::getNotificationId, ids)
                .isNull(NotificationRead::getDeletedAt));
        long readCount = readCnt == null ? 0 : readCnt;
        return ids.size() - readCount;
    }

    // ============================== 详情（自动已读） ==============================

    /**
     * 通知详情（校验属于当前用户），同时自动标记已读（upsert notification_read）。
     */
    @Transactional
    public NotificationVO detail(Long id) {
        Long userId = SecurityUtils.currentUserId();
        Notification n = getOwnedOrThrow(id, userId);

        // 自动标记已读
        markRead(id, userId);

        NotificationVO vo = toVO(n, true);
        return vo;
    }

    // ============================== 标记已读 ==============================

    /**
     * 标记单条通知已读（校验属于当前用户）。幂等。
     */
    @Transactional
    public void markRead(Long id) {
        Long userId = SecurityUtils.currentUserId();
        getOwnedOrThrow(id, userId);
        markRead(id, userId);
    }

    /**
     * 批量已读：当前用户全部未读通知一次性插入 notification_read；type 可选过滤。
     * 返回标记数量。
     */
    @Transactional
    public int markAllRead(String type) {
        Long userId = SecurityUtils.currentUserId();

        // 当前用户符合 type 条件的通知ID
        List<Notification> mine = notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .select(Notification::getId)
                .eq(Notification::getUserId, userId)
                .eq(type != null && !type.isBlank(), Notification::getType, type)
                .isNull(Notification::getDeletedAt));
        if (mine.isEmpty()) {
            return 0;
        }
        Set<Long> allIds = mine.stream().map(Notification::getId).collect(Collectors.toSet());

        // 已读过的（排除，避免重复插入冲突）
        Set<Long> alreadyRead = loadReadIds(userId, allIds);
        Set<Long> toRead = new HashSet<>(allIds);
        toRead.removeAll(alreadyRead);
        if (toRead.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Long notifId : toRead) {
            NotificationRead r = new NotificationRead();
            r.setNotificationId(notifId);
            r.setUserId(userId);
            r.setReadAt(now);
            try {
                notificationReadMapper.insert(r);
            } catch (Exception e) {
                // 并发下 UNIQUE 冲突视为已读，跳过
                log.debug("notification_read 已存在，跳过：notifId={}, userId={}", notifId, userId);
            }
        }
        return toRead.size();
    }

    // ============================== 管理员创建（预留） ==============================

    /**
     * 管理员创建系统通知（按用户维度），返回通知ID。
     */
    @Transactional
    public Long create(CreateNotificationRequest req) {
        Notification n = new Notification();
        n.setUserId(req.getUserId());
        n.setType((req.getType() == null || req.getType().isBlank()) ? "system" : req.getType());
        n.setTitle(req.getTitle());
        n.setContent(req.getContent() == null ? "" : req.getContent());
        n.setLink(req.getLink());
        notificationMapper.insert(n);
        return n.getId();
    }

    // ============================== 内部 ==============================

    /**
     * 取得当前用户可见的通知，校验归属（user_id 必须等于当前用户），否则抛业务异常。
     */
    private Notification getOwnedOrThrow(Long id, Long userId) {
        Notification n = notificationMapper.selectById(id);
        if (n == null || n.getDeletedAt() != null || !userId.equals(n.getUserId())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "通知不存在");
        }
        return n;
    }

    /**
     * 单条通知 upsert 已读（UNIQUE notification_id + user_id）。
     * 查既有记录（含软删，deletedAt select=false 需绕过），存在则恢复，否则新增。
     */
    private void markRead(Long notificationId, Long userId) {
        QueryWrapper<NotificationRead> qw = new QueryWrapper<>();
        qw.eq("notification_id", notificationId).eq("user_id", userId).last("LIMIT 1");
        NotificationRead exist = notificationReadMapper.selectOne(qw);

        if (exist != null) {
            if (exist.getDeletedAt() != null) {
                exist.setDeletedAt(null);
                exist.setReadAt(LocalDateTime.now());
                notificationReadMapper.updateById(exist);
            }
            // 已存在且未软删 → 已读，无需操作
            return;
        }
        NotificationRead r = new NotificationRead();
        r.setNotificationId(notificationId);
        r.setUserId(userId);
        r.setReadAt(LocalDateTime.now());
        try {
            notificationReadMapper.insert(r);
        } catch (Exception e) {
            // 并发下 UNIQUE 冲突视为已读
            log.debug("notification_read 已存在，跳过：notifId={}, userId={}", notificationId, userId);
        }
    }

    /**
     * 加载当前用户在指定通知集合中的已读通知ID。
     */
    private Set<Long> loadReadIds(Long userId, Set<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<NotificationRead> reads = notificationReadMapper.selectList(new LambdaQueryWrapper<NotificationRead>()
                .select(NotificationRead::getNotificationId)
                .eq(NotificationRead::getUserId, userId)
                .in(NotificationRead::getNotificationId, notificationIds)
                .isNull(NotificationRead::getDeletedAt));
        return reads.stream().map(NotificationRead::getNotificationId).collect(Collectors.toSet());
    }

    private NotificationVO toVO(Notification n, boolean read) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setType(n.getType());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setLink(n.getLink());
        vo.setRead(read);
        vo.setCreatedAt(n.getCreatedAt());
        return vo;
    }
}
