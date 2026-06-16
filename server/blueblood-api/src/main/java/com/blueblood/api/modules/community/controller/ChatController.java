package com.blueblood.api.modules.community.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.community.dto.ChatMessageVO;
import com.blueblood.api.modules.community.dto.ChatSessionVO;
import com.blueblood.api.modules.community.dto.SendMessageRequest;
import com.blueblood.api.modules.community.service.ChatService;
import com.blueblood.api.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 私信（Chat）模块：会话列表、消息分页、发送消息。
 * 所有接口默认需登录（SecurityUtils.currentUserId()）。
 */
@Tag(name = "私信", description = "私信会话与消息")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "我的会话列表")
    @GetMapping("/sessions")
    public Result<List<ChatSessionVO>> sessions() {
        return Result.success(chatService.mySessions(SecurityUtils.currentUserId()));
    }

    @Operation(summary = "与某人的消息分页")
    @GetMapping("/messages")
    public Result<PageResult<ChatMessageVO>> messages(
            @Parameter(description = "对方用户ID", required = true)
            @RequestParam Long peerId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        return Result.success(chatService.pageMessages(SecurityUtils.currentUserId(), peerId, page, pageSize));
    }

    @Operation(summary = "发送私信")
    @PostMapping("/messages")
    public Result<ChatMessageVO> send(@Valid @RequestBody SendMessageRequest req) {
        return Result.success(chatService.sendMessage(SecurityUtils.currentUserId(), req));
    }
}
