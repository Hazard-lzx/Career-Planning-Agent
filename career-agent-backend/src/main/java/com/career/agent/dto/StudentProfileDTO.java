package com.career.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 学生能力画像DTO
 * 严格对应全局规范中的JSON结构
 */
@Data
@Schema(description = "学生能力画像")
public class StudentProfileDTO {

    @Schema(description = "教育背景")
    private Education education;

    @Schema(description = "技能列表")
    private List<String> skills;

    @Schema(description = "证书列表")
    private List<String> certificates;

    @Schema(description = "软技能")
    private Map<String, String> softSkills;

    @Schema(description = "实习经历")
    private List<Internship> internships;

    @Schema(description = "项目经历")
    private List<Project> projects;

    /**
     * 教育背景
     */
    @Data
    @Schema(description = "教育背景")
    public static class Education {
        @Schema(description = "学历")
        private String degree;

        @Schema(description = "专业")
        private String major;

        @Schema(description = "学校")
        private String school;
    }

    /**
     * 实习经历
     */
    @Data
    @Schema(description = "实习经历")
    public static class Internship {
        @Schema(description = "公司")
        private String company;

        @Schema(description = "职位")
        private String position;

        @Schema(description = "时长")
        private String duration;

        @Schema(description = "描述")
        private String description;
    }

    /**
     * 项目经历
     */
    @Data
    @Schema(description = "项目经历")
    public static class Project {
        @Schema(description = "项目名称")
        private String name;

        @Schema(description = "角色")
        private String role;

        @Schema(description = "描述")
        private String description;
    }
}
