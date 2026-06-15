package com.career.agent.controller;

import com.career.agent.common.Result;
import com.career.agent.dto.MatchRequestDTO;
import com.career.agent.dto.MatchResultDTO;
import com.career.agent.service.MatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 人岗匹配控制器
 * 接口前缀：/api/match
 */
@RestController
@RequestMapping("/api/match")
@Tag(name = "人岗匹配")
@CrossOrigin
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    /**
     * 计算人岗匹配
     * 请求体包含学生ID、岗位ID和各维度权重
     * 返回匹配结果（total_score, dimensions, gap_analysis, matched_skills, missing_skills）
     */
    @PostMapping("/calculate")
    @Operation(summary = "计算人岗匹配得分与差距分析")
    public Result<MatchResultDTO> calculate(@RequestBody MatchRequestDTO requestDTO) {
        MatchResultDTO result = matchService.calculate(requestDTO);
        return Result.success(result);
    }
}
