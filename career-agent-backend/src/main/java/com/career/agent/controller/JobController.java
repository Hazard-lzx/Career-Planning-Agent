package com.career.agent.controller;

import com.career.agent.common.Result;
import com.career.agent.dto.JobQueryDTO;
import com.career.agent.dto.PageResultDTO;
import com.career.agent.entity.Job;
import com.career.agent.service.JobService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 岗位管理控制器
 * 接口前缀：/api/job
 */
@RestController
@RequestMapping("/api/job")
@Tag(name = "岗位管理")
@CrossOrigin
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /**
     * 分页查询岗位列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询岗位列表")
    public Result<PageResultDTO<Job>> listJobs(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        JobQueryDTO queryDTO = new JobQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setKeyword(keyword);
        PageResultDTO<Job> result = jobService.listJobs(queryDTO);
        return Result.success(result);
    }

    /**
     * 获取岗位详情（包含所有字段及profile_json）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取岗位详情")
    public Result<Job> getJobDetail(
            @Parameter(description = "岗位ID", required = true)
            @PathVariable Long id) {
        Job job = jobService.getJobDetail(id);
        return Result.success(job);
    }

    /**
     * 批量导入岗位数据（CSV文件）
     * 导入后通过RocketMQ异步生成画像和构建图谱
     */
@PostMapping(value = "/batch-import", consumes = {"multipart/form-data"})
    @Operation(summary = "批量导入岗位数据", description = "导入成功后异步生成画像和图谱")
    public Result<?> batchImport(
            @Parameter(description = "CSV文件", required = true)
            @RequestParam("file") MultipartFile file) {

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".csv"))) {
            return Result.fail("请上传 CSV 格式的文件");
        }

        try {
            int count = jobService.batchImport(file);
            return Result.success("成功导入" + count + "条岗位数据，画像和图谱构建中", count);
        } catch (Exception e) {
            return Result.fail("导入失败：" + e.getMessage());
        }
    }

    /**
     * 生成岗位画像（调用AI解析岗位描述）
     */
    @PostMapping("/{id}/generate-profile")
    @Operation(summary = "生成岗位画像")
    public Result<Job> generateProfile(
            @Parameter(description = "岗位ID", required = true)
            @PathVariable Long id) {
        Job job = jobService.getJobDetail(id);
        jobService.generateJobProfile(job);
        // 重新查询获取更新后的画像
        Job updated = jobService.getJobDetail(id);
        return Result.success(updated);
    }
}
