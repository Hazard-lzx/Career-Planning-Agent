package com.career.agent.agent;

import com.career.agent.dto.MatchResultDTO;
import com.career.agent.dto.PathNodeDTO;
import com.career.agent.entity.Job;
import com.career.agent.entity.Report;
import com.career.agent.entity.Student;
import com.career.agent.mapper.JobMapper;
import com.career.agent.mapper.StudentMapper;
import com.career.agent.service.JobService;
import com.career.agent.service.MatchService;
import com.career.agent.service.ReportService;
import com.career.agent.service.StudentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Agent 配置类
 */
@Slf4j
@Configuration
public class AgentConfig {

    @Component
    public static class AgentTools {

        private final MatchService matchService;
        private final JobService jobService;
        private final ReportService reportService;
        private final StudentService studentService;
        private final StudentMapper studentMapper;
        private final JobMapper jobMapper;
        private final ObjectMapper objectMapper;

        public AgentTools(MatchService matchService, JobService jobService,
                          ReportService reportService, StudentService studentService,
                          StudentMapper studentMapper, JobMapper jobMapper,
                          ObjectMapper objectMapper) {
            this.matchService = matchService;
            this.jobService = jobService;
            this.reportService = reportService;
            this.studentService = studentService;
            this.studentMapper = studentMapper;
            this.jobMapper = jobMapper;
            this.objectMapper = objectMapper;
        }

        @Tool("根据姓名关键词搜索学生，返回匹配的学生列表（含ID和基本信息）")
        public String searchStudents(String keyword) {
            List<Student> list = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .like(Student::getName, keyword)
                            .or().like(Student::getRawResumeText, keyword)
                            .last("LIMIT 5"));
            try { return objectMapper.writeValueAsString(list.stream().map(s ->
                    Map.of("id", s.getId(), "name", s.getName(), "email", s.getEmail() != null ? s.getEmail() : "")
            ).toList()); }
            catch (JsonProcessingException e) { return "[]"; }
        }

        @Tool("根据关键词搜索岗位，返回匹配的岗位列表（含ID、名称、公司、行业）")
        public String searchJobs(String keyword) {
            List<Job> list = jobMapper.selectList(
                    new LambdaQueryWrapper<Job>()
                            .like(Job::getTitle, keyword)
                            .or().like(Job::getIndustry, keyword)
                            .or().like(Job::getCompanyFullName, keyword)
                            .last("LIMIT 5"));
            try { return objectMapper.writeValueAsString(list.stream().map(j ->
                    Map.of("id", j.getId(), "title", j.getTitle(), "company", j.getCompanyFullName() != null ? j.getCompanyFullName() : "",
                            "industry", j.getIndustry() != null ? j.getIndustry() : "", "location", j.getWorkLocation() != null ? j.getWorkLocation() : "")
            ).toList()); }
            catch (JsonProcessingException e) { return "[]"; }
        }

        @Tool("获取学生能力画像，返回教育背景、技能、证书等信息")
        public String getStudentProfile(long studentId) {
            Student s = studentService.getProfile(studentId);
            try { return objectMapper.writeValueAsString(s); }
            catch (JsonProcessingException e) { return "获取失败: " + e.getMessage(); }
        }

        @Tool("评估学生与岗位的匹配度，返回总分、各维度得分、差距分析、匹配和缺失技能")
        public String calculateMatch(long studentId, long jobId) {
            MatchResultDTO r = matchService.calculateMatch(studentId, jobId);
            try { return objectMapper.writeValueAsString(r); }
            catch (JsonProcessingException e) { return "匹配失败: " + e.getMessage(); }
        }

        @Tool("获取岗位的职业发展路径，包含垂直晋升和水平换岗路径")
        public String getCareerPaths(long jobId) {
            Map<String, List<PathNodeDTO>> paths = jobService.getCareerPaths(jobId);
            try { return objectMapper.writeValueAsString(paths); }
            catch (JsonProcessingException e) { return "查询失败: " + e.getMessage(); }
        }

        @Tool("生成完整的职业规划报告，包含自我认知、岗位剖析、匹配分析、发展路径和行动计划")
        public String generateReport(long studentId, long jobId) {
            Report r = reportService.generateReport(studentId, jobId);
            try { return objectMapper.writeValueAsString(r); }
            catch (JsonProcessingException e) { return "生成失败: " + e.getMessage(); }
        }
    }

    @Bean
    AgentService agentService(ChatLanguageModel model, AgentTools tools) {
        return AiServices.builder(AgentService.class)
                .chatLanguageModel(model)
                .tools(tools)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }
}
