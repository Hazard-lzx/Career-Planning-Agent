package com.career.agent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 匹配结果DTO
 * 严格对应全局规范中的JSON结构
 */
@Data
@Schema(description = "匹配结果")
public class MatchResultDTO {

    @JsonProperty("total_score")
    @Schema(description = "总分")
    private Double totalScore;

    @Schema(description = "各维度得分")
    private Dimensions dimensions;

    @JsonProperty("gap_analysis")
    @Schema(description = "差距分析")
    private String gapAnalysis;

    @JsonProperty("matched_skills")
    @Schema(description = "已匹配技能")
    private List<String> matchedSkills;

    @JsonProperty("missing_skills")
    @Schema(description = "缺失技能")
    private List<String> missingSkills;

    /**
     * 各维度得分
     */
    @Data
    @Schema(description = "匹配维度得分")
    public static class Dimensions {
        @Schema(description = "基本条件匹配度")
        private Double basic;

        @JsonProperty("hard_skill")
        @Schema(description = "硬技能匹配度")
        private Double hardSkill;

        @JsonProperty("soft_skill")
        @Schema(description = "软技能匹配度")
        private Double softSkill;

        @Schema(description = "潜力评估")
        private Double potential;
    }
}
