package com.blueblood.api.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.admin.dto.AdminNotificationQuery;
import com.blueblood.api.modules.admin.dto.AdminNotificationVO;
import com.blueblood.api.modules.admin.dto.NotificationRequest;
import com.blueblood.api.modules.notification.entity.Notification;
import com.blueblood.api.modules.notification.mapper.NotificationMapper;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 后台通知管理：全站通知分页、发送通知、软删。
 */
@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;

    public PageResult<AdminNotificationVO> page(AdminNotificationQuery query) {
        Page<Notification> page = query.toPage();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .isNull(Notification::getDeletedAt)
                .orderByDesc(Notification::getCreatedAt);

        if (query.getUserId() != null) {
            wrapper.eq(Notification::getUserId, query.getUserId());
        }
        if (StringUtils.hasText(query.getType())) {
            wrapper.eq(Notification::getType, query.getType());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(Notification::getTitle, kw)
                    .or().like(Notification::getContent, kw));
        }

        Page<Notification> result = notificationMapper.selectPage(page, wrapper);

        // 批量查询接收人昵称
        Set<Long> userIds = result.getRecords().stream()
                .map(Notification::getUserId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> nicknameMap = loadNicknames(userIds);

        return PageResult.of(result.convert(n -> toVO(n, nicknameMap)));
    }

    /** 后台发送通知：插入一条通知记录。 */
    @Transactional
    public void send(NotificationRequest req) {
        // 校验接收人存在
        User user = userMapper.selectById(req.getUserId());
        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "接收用户不存在");
        }

        Notification n = new Notification();
        n.setUserId(req.getUserId());
        n.setType(req.getType());
        n.setTitle(req.getTitle());
        n.setContent(req.getContent());
        n.setLink(req.getLink());
        notificationMapper.insert(n);
    }

    /** 软删通知。 */
    @Transactional
    public void delete(Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n == null || n.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "通知不存在");
        }
        n.setDeletedAt(LocalDateTime.now());
        notificationMapper.updateById(n);
    }

    private Map<Long, String> loadNicknames(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return new HashMap<>();
        }
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .in(User::getId, userIds)
                .isNull(User::getDeletedAt));
        Map<Long, String> map = new HashMap<>(users.size() * 2);
        for (User u : users) {
            map.put(u.getId(), u.getNickname());
        }
        return map;
    }

    private AdminNotificationVO toVO(Notification n, Map<Long, String> nicknameMap) {
        AdminNotificationVO vo = new AdminNotificationVO();
        vo.setId(n.getId());
        vo.setUserId(n.getUserId());
        vo.setRecipientNickname(n.getUserId() == null ? null : nicknameMap.get(n.getUserId()));
        vo.setType(n.getType());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setLink(n.getLink());
        vo.setCreatedAt(n.getCreatedAt());
        return vo;
    }
}
