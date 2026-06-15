package com.career.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 人岗匹配请求DTO
 */
@Data
@Schema(description = "人岗匹配请求")
public class MatchRequestDTO {

    @Schema(description = "学生ID", required = true)
    private Long studentId;

    @Schema(description = "岗位ID", required = true)
    private Long jobId;

    @Schema(description = "各维度权重")
    private Map<String, Double> weights;
}
