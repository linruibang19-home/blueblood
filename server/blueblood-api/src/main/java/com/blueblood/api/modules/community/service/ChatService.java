package com.blueblood.api.modules.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.community.dto.ChatMessageVO;
import com.blueblood.api.modules.community.dto.ChatSessionVO;
import com.blueblood.api.modules.community.dto.SendMessageRequest;
import com.blueblood.api.modules.community.entity.ChatMessage;
import com.blueblood.api.modules.community.entity.ChatSession;
import com.blueblood.api.modules.community.mapper.ChatMessageMapper;
import com.blueblood.api.modules.community.mapper.ChatSessionMapper;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 私信服务。
 * <p>
 * 会话采用「单向维护」：当前用户(me) 向 对方(peer) 发送消息时，仅维护 user_id=me、peer_id=peer 的这一条会话。
 * 对方收到的未读统计由对方自己的会话记录维护（对方主动发消息或后续按需扩展时创建）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;

    // ============================== 会话列表 ==============================

    /**
     * 我的会话列表，按 last_message_time 倒序。
     */
    public List<ChatSessionVO> mySessions(Long me) {
        List<ChatSession> sessions = chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, me)
                .isNull(ChatSession::getDeletedAt)
                .eq(ChatSession::getStatus, "ACTIVE")
                .orderByDesc(ChatSession::getLastMessageTime));

        if (sessions.isEmpty()) {
            return List.of();
        }

        // 批量取对方用户，避免 N+1
        List<Long> peerIds = sessions.stream().map(ChatSession::getPeerId).distinct().toList();
        Map<Long, User> peerMap = new HashMap<>();
        for (Long pid : peerIds) {
            User u = userMapper.selectById(pid);
            if (u != null && u.getDeletedAt() == null) {
                peerMap.put(pid, u);
            }
        }

        return sessions.stream().map(s -> toSessionVO(s, peerMap.get(s.getPeerId()))).toList();
    }

    private ChatSessionVO toSessionVO(ChatSession s, User peer) {
        ChatSessionVO vo = new ChatSessionVO();
        vo.setSessionId(s.getId());
        vo.setPeerId(s.getPeerId());
        if (peer != null) {
            vo.setPeerName(peer.getNickname());
            vo.setPeerAvatar(peer.getAvatar());
        }
        vo.setLastMessage(s.getLastMessage());
        vo.setLastMessageTime(s.getLastMessageTime());
        vo.setUnreadCount(s.getUnreadCount() == null ? 0 : s.getUnreadCount());
        return vo;
    }

    // ============================== 消息分页 ==============================

    /**
     * 与某人的消息分页。
     * 以 user_id=我 且 peer_id=对方 的 session 为准；会话不存在则返回空列表。
     * 排序默认按 created_time 倒序（最新在前），便于前端「下拉加载历史」。
     */
    public PageResult<ChatMessageVO> pageMessages(Long me, Long peerId, Integer page, Integer pageSize) {
        ChatSession session = findActiveSession(me, peerId);
        if (session == null) {
            return PageResult.of(new Page<>());
        }

        int p = page == null || page < 1 ? 1 : page;
        int size = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);

        Page<ChatMessage> pg = new Page<>(p, size);
        Page<ChatMessage> result = chatMessageMapper.selectPage(pg, new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getSessionId, session.getId())
                .isNull(ChatMessage::getDeletedAt)
                .orderByDesc(ChatMessage::getCreatedAt));

        PageResult<ChatMessage> pr = PageResult.of(result);
        List<ChatMessageVO> list = pr.getList().stream().map(this::toMessageVO).toList();
        return new PageResult<>(list, pr.getTotal(), pr.getPage(), pr.getPageSize());
    }

    private ChatMessageVO toMessageVO(ChatMessage m) {
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(m.getId());
        vo.setSenderId(m.getSenderId());
        vo.setContent(m.getContent());
        vo.setType(m.getType());
        vo.setIsRead(m.getIsRead() == null ? 0 : m.getIsRead());
        vo.setCreatedAt(m.getCreatedAt());
        return vo;
    }

    // ============================== 发送消息 ==============================

    /**
     * 发送消息：upsert 我→对方方向的会话，插入消息，返回新消息。
     * 单向维护：不主动为对方创建对向会话。
     */
    @Transactional
    public ChatMessageVO sendMessage(Long me, SendMessageRequest req) {
        Long peerId = req.getReceiverId();

        // 不能给自己发私信
        if (peerId.equals(me)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能给自己发送私信");
        }

        // 校验对方存在
        User peer = userMapper.selectById(peerId);
        if (peer == null || peer.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "接收者不存在");
        }

        String type = (req.getType() == null || req.getType().isBlank()) ? "text" : req.getType();
        LocalDateTime now = LocalDateTime.now();

        // upsert 我→对方 会话（单向）
        ChatSession session = findActiveSession(me, peerId);
        if (session == null) {
            session = new ChatSession();
            session.setUserId(me);
            session.setPeerId(peerId);
            session.setLastMessage(req.getContent());
            session.setLastMessageTime(now);
            session.setUnreadCount(0); // 我自己发的，对方未读不在本会话统计
            session.setStatus("ACTIVE");
            chatSessionMapper.insert(session);
        } else {
            ChatSession patch = new ChatSession();
            patch.setId(session.getId());
            patch.setLastMessage(req.getContent());
            patch.setLastMessageTime(now);
            patch.setStatus("ACTIVE");
            chatSessionMapper.updateById(patch);
        }

        // 插入消息
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(session.getId());
        msg.setSenderId(me);
        msg.setReceiverId(peerId);
        msg.setContent(req.getContent());
        msg.setType(type);
        msg.setIsRead(0);
        chatMessageMapper.insert(msg);

        return toMessageVO(msg);
    }

    // ============================== 内部 ==============================

    /** 查找当前用户视角下的活跃会话（单向：user_id=me, peer_id=peer）。不存在返回 null。 */
    private ChatSession findActiveSession(Long me, Long peerId) {
        return chatSessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, me)
                .eq(ChatSession::getPeerId, peerId)
                .isNull(ChatSession::getDeletedAt)
                .last("LIMIT 1"));
    }
}
