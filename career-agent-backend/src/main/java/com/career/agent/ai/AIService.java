package com.career.agent.ai;

import com.career.agent.dto.MatchResultDTO;
import com.career.agent.entity.Job;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 核心 AI 服务接口
 */
public interface AIService {

    StudentProfile parseResume(String resumeText);
    JobProfile parseJob(String jobDescription, String title);
    MatchResultDTO match(StudentProfile sp, JobProfile jp, Map<String, Double> weights);
    String polish(String rawText, String section);
    void buildJobGraph(List<Job> jobs);

    // ===== 数据类 (支持 snake_case + camelCase JSON 映射) =====

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class StudentProfile {
        private Education education = new Education();
        private List<String> skills = List.of();
        private List<String> certificates = List.of();
        @JsonAlias("soft_skills")
    private Map<String, String> softSkills = Map.of();
        private List<Map<String, Object>> internships = List.of();
        private List<Map<String, Object>> projects = List.of();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class Education {
        private String degree = "";
        private String major = "";
        private String school = "";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class JobProfile {
        @JsonAlias("basic_requirements")
    private BasicRequirements basicRequirements = new BasicRequirements();
        @JsonAlias("hard_skills")
    private List<String> hardSkills = List.of();
        @JsonAlias("soft_skills")
    private Map<String, String> softSkills = Map.of();
        private List<String> certificates = List.of();
        private String experience = "";
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class BasicRequirements {
        private String degree = "";
        private String major = "";
    }
}
