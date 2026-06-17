package com.blueblood.api.modules.task.controller;

import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.task.dto.TaskCategoryVO;
import com.blueblood.api.modules.task.dto.TaskListItemVO;
import com.blueblood.api.modules.task.dto.TaskOrderVO;
import com.blueblood.api.modules.task.dto.TaskVO;
import com.blueblood.api.modules.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 任务模块：分类、大厅、详情、接单、我的任务、执行详情。
 */
@Tag(name = "任务", description = "任务大厅、接单、我的任务、执行详情")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

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
}
