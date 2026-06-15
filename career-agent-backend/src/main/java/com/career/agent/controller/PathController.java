package com.career.agent.controller;

import com.career.agent.common.Result;
import com.career.agent.dto.PathNodeDTO;
import com.career.agent.service.PathService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职业路径控制器
 * 接口前缀：/api/path
 * 从Neo4j知识图谱中查询职业发展路径
 */
@RestController
@RequestMapping("/api/path")
@Tag(name = "职业路径")
@CrossOrigin
@RequiredArgsConstructor
public class PathController {

    private final PathService pathService;

    /**
     * 获取垂直晋升路径
     */
    @GetMapping("/vertical/{jobId}")
    @Operation(summary = "获取垂直晋升路径")
    public Result<List<PathNodeDTO>> getVerticalPath(
            @Parameter(description = "岗位ID", required = true)
            @PathVariable Long jobId) {
        List<PathNodeDTO> path = pathService.getVerticalPath(jobId);
        return Result.success(path);
    }

    /**
     * 获取水平换岗路径（至少2条）
     */
    @GetMapping("/horizontal/{jobId}")
    @Operation(summary = "获取水平换岗路径")
    public Result<List<PathNodeDTO>> getHorizontalPath(
            @Parameter(description = "岗位ID", required = true)
            @PathVariable Long jobId) {
        List<PathNodeDTO> path = pathService.getHorizontalPath(jobId);
        return Result.success(path);
    }
}
