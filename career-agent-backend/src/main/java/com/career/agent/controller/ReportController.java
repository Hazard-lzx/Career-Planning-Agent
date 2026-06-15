package com.career.agent.controller;

import com.career.agent.common.Result;
import com.career.agent.dto.ReportGenerateDTO;
import com.career.agent.dto.ReportPolishDTO;
import com.career.agent.entity.Report;
import com.career.agent.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报告管理控制器
 * 接口前缀：/api/report
 */
@RestController
@RequestMapping("/api/report")
@Tag(name = "报告管理")
@CrossOrigin
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 生成职业规划报告（异步）
     * 立即返回报告ID和status=pending，由RocketMQ消费者异步完成生成
     * 前端通过轮询GET /api/report/{id}获取状态，status=complete时展示报告
     */
    @PostMapping("/generate")
    @Operation(summary = "生成职业规划报告（异步）", description = "立即返回pending状态，通过轮询获取结果")
    public Result<Report> generate(@RequestBody ReportGenerateDTO generateDTO) {
        Report report = reportService.generate(generateDTO.getStudentId(), generateDTO.getJobId());
        return Result.success(report);
    }

    /**
     * 获取报告
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取报告详情")
    public Result<Report> getReport(
            @Parameter(description = "报告ID", required = true)
            @PathVariable Long id) {
        Report report = reportService.getReport(id);
        return Result.success(report);
    }

    /**
     * 按学生ID查询报告列表
     */
    @GetMapping("/list/{studentId}")
    @Operation(summary = "查询学生的报告列表")
    public Result<List<Report>> listByStudentId(
            @Parameter(description = "学生ID", required = true)
            @PathVariable Long studentId) {
        List<Report> reports = reportService.listByStudentId(studentId);
        return Result.success(reports);
    }

    /**
     * 更新报告内容
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新报告内容")
    public Result<Report> updateContent(
            @Parameter(description = "报告ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "报告内容", required = true)
            @RequestBody String content) {
        Report report = reportService.updateContent(id, content);
        return Result.success(report);
    }

    /**
     * 润色报告单段内容
     */
    @PostMapping("/{id}/polish")
    @Operation(summary = "润色报告单段内容")
    public Result<String> polishSection(
            @Parameter(description = "报告ID", required = true)
            @PathVariable Long id,
            @RequestBody ReportPolishDTO polishDTO) {
        String polishedText = reportService.polishSection(id, polishDTO);
        return Result.success(polishedText);
    }

    /**
     * 导出报告为Word文档
     */
    @GetMapping("/{id}/export")
    @Operation(summary = "导出报告为Word文档")
    public ResponseEntity<byte[]> exportWord(
            @Parameter(description = "报告ID", required = true)
            @PathVariable Long id) {
        byte[] docBytes = reportService.exportWord(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=career_report_" + id + ".docx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(docBytes);
    }

    /**
     * 删除报告
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除报告")
    public Result<Void> deleteReport(
            @Parameter(description = "报告ID", required = true)
            @PathVariable Long id) {
        reportService.deleteReport(id);
        return Result.success("报告已删除", null);
    }
}
