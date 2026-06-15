package com.career.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 岗位要求画像DTO
 * 严格对应全局规范中的JSON结构
 */
@Data
@Schema(description = "岗位要求画像")
public class JobProfileDTO {

    @Schema(description = "基本要求")
    private BasicRequirements basicRequirements;

    @Schema(description = "硬技能列表")
    private List<String> hardSkills;

    @Schema(description = "软技能")
    private Map<String, String> softSkills;

    @Schema(description = "证书要求")
    private List<String> certificates;

    @Schema(description = "经验要求")
    private String experience;

    /**
     * 基本要求
     */
    @Data
    @Schema(description = "基本要求")
    public static class BasicRequirements {
        @Schema(description = "学历要求")
        private String degree;

        @Schema(description = "专业要求")
        private String major;
    }
}
