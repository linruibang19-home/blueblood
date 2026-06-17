package com.blueblood.api.modules.system.controller;

import com.blueblood.api.common.result.Result;
import com.blueblood.api.modules.system.dto.FileVO;
import com.blueblood.api.modules.system.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传：上传、查看、删除。
 */
@Tag(name = "文件", description = "文件上传、查看、删除")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文件(multipart)")
    @PostMapping("/upload")
    public Result<FileVO> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "bizType", required = false) String bizType) {
        return Result.success(fileService.upload(file, bizType));
    }

    @Operation(summary = "查看文件信息")
    @GetMapping("/{id}")
    public Result<FileVO> info(@PathVariable Long id) {
        return Result.success(fileService.info(id));
    }

    @Operation(summary = "删除文件(软删)")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return Result.success();
    }
}
