package com.blueblood.api.modules.task.controller;

import com.blueblood.api.common.idempotent.IdempotentService;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.task.dto.MilestoneReviewVO;
import com.blueblood.api.modules.task.dto.PublishTaskRequest;
import com.blueblood.api.modules.task.dto.TaskCategoryVO;
import com.blueblood.api.modules.task.dto.TaskListItemVO;
import com.blueblood.api.modules.task.dto.TaskOrderVO;
import com.blueblood.api.modules.task.dto.TaskVO;
import com.blueblood.api.modules.task.service.TaskPublishService;
import com.blueblood.api.modules.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务模块：分类、大厅、详情、接单、我的任务、执行详情、发布、雇主工作台。
 */
@Tag(name = "任务", description = "任务大厅、接单、我的任务、执行详情、发布、雇主工作台")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TaskPublishService taskPublishService;
    private final IdempotentService idempotentService;

    @Operation(summary = "任务分类列表")
    @GetMapping("/categories")
    public Result<List<TaskCategoryVO>> categories() {
        return Result.success(taskService.categories());
    }

    @Operation(summary = "任务大厅(筛选分页)")
    @GetMapping
    public Result<PageResult<TaskListItemVO>> list(@RequestParam(required = false) String category,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer pageSize) {
        return Result.success(taskService.pageTasks(category, keyword, status, page, pageSize));
    }

    @Operation(summary = "任务详情")
    @GetMapping("/{id}")
    public Result<TaskVO> detail(@PathVariable Long id) {
        return Result.success(taskService.detail(id));
    }

    @Operation(summary = "确认接单")
    @PostMapping("/{id}/accept")
    public Result<Map<String, Long>> accept(@PathVariable Long id) {
        return Result.success(taskService.accept(id));
    }

    @Operation(summary = "我的任务订单")
    @GetMapping("/orders/mine")
    public Result<PageResult<TaskOrderVO>> myOrders(@RequestParam(required = false) String status,
                                                    @RequestParam(required = false) Integer page,
                                                    @RequestParam(required = false) Integer pageSize) {
        return Result.success(taskService.myOrders(status, page, pageSize));
    }

    @Operation(summary = "任务执行详情(含里程碑)")
    @GetMapping("/orders/{orderId}")
    public Result<TaskOrderVO> orderDetail(@PathVariable Long orderId) {
        return Result.success(taskService.orderDetail(orderId));
    }

    // ============================== 用户端发布(Q1 企业/个人 + Q2 直接上架) ==============================

    @Operation(summary = "获取一次性幂等 token(发布任务防重提交)")
    @GetMapping("/idempotent-token")
    public Result<Map<String, String>> idempotentToken(@RequestParam(defaultValue = "publish") String biz) {
        return Result.success(Map.of("token", idempotentService.issue(biz)));
    }

    @Operation(summary = "发布任务(企业/个人,需带 X-Idempotent-Token 头)")
    @PostMapping
    public Result<Long> publish(@RequestHeader(value = "X-Idempotent-Token", required = false) String token,
                                @Valid @RequestBody PublishTaskRequest req) {
        return Result.success(taskPublishService.publish(req, token));
    }

    @Operation(summary = "编辑我发布的任务(仅雇主,接单前)")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PublishTaskRequest req) {
        taskPublishService.update(id, req);
        return Result.success();
    }

    @Operation(summary = "下架我发布的任务")
    @PutMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        taskPublishService.close(id);
        return Result.success();
    }

    @Operation(summary = "我发布的任务列表")
    @GetMapping("/published")
    public Result<PageResult<TaskListItemVO>> pagePublished(@RequestParam(required = false) String status,
                                                            @RequestParam(required = false) Integer page,
                                                            @RequestParam(required = false) Integer pageSize) {
        return Result.success(taskPublishService.pagePublished(status, page, pageSize));
    }

    @Operation(summary = "待我审核的里程碑(雇主工作台)")
    @GetMapping("/published/milestones")
    public Result<PageResult<MilestoneReviewVO>> pageMilestonesForReview(@RequestParam(required = false) String status,
                                                                          @RequestParam(required = false) Integer page,
                                                                          @RequestParam(required = false) Integer pageSize) {
        return Result.success(taskPublishService.pageMilestonesForReview(status, page, pageSize));
    }
}
