package com.blueblood.api.modules.community.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.community.dto.GroupVO;
import com.blueblood.api.modules.community.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小组模块：列表 / 详情 / 我加入的 / 加入 / 退出。
 */
@Tag(name = "小组", description = "兴趣小组：列表、详情、加入与退出")
@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "小组分页列表（可按 category 过滤）")
    @GetMapping
    public Result<PageResult<GroupVO>> page(@RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer pageSize,
                                            @RequestParam(required = false) String category) {
        return Result.success(groupService.page(page, pageSize, category));
    }

    @Operation(summary = "小组详情（含 joined）")
    @GetMapping("/{id}")
    public Result<GroupVO> detail(@PathVariable Long id) {
        return Result.success(groupService.detail(id));
    }

    @Operation(summary = "我加入的小组列表")
    @GetMapping("/mine")
    public Result<List<GroupVO>> mine() {
        return Result.success(groupService.mine());
    }

    @Operation(summary = "加入小组")
    @PostMapping("/{id}/join")
    public Result<Void> join(@PathVariable Long id) {
        groupService.join(id);
        return Result.success();
    }

    @Operation(summary = "退出小组")
    @PostMapping("/{id}/leave")
    public Result<Void> leave(@PathVariable Long id) {
        groupService.leave(id);
        return Result.success();
    }
}
