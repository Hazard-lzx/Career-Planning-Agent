package com.career.agent.service.impl;

import com.career.agent.ai.AIService;
import com.career.agent.common.BusinessException;
import com.career.agent.dto.MatchRequestDTO;
import com.career.agent.dto.MatchResultDTO;
import com.career.agent.entity.Job;
import com.career.agent.entity.Student;
import com.career.agent.mapper.JobMapper;
import com.career.agent.mapper.StudentMapper;
import com.career.agent.service.MatchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 人岗匹配服务实现类 (LangChain4j 版本)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchServiceImpl implements MatchService {

    private final StudentMapper studentMapper;
    private final JobMapper jobMapper;
    private final AIService aiService;
    private final ObjectMapper objectMapper;

    @Override
    public MatchResultDTO calculate(MatchRequestDTO dto) {
        Student student = getStudent(dto.getStudentId());
        Job job = getJob(dto.getJobId());
        return doMatch(student, job, dto.getWeights());
    }

    @Override
    public MatchResultDTO calculateMatch(Long studentId, Long jobId) {
        Student student = getStudent(studentId);
        Job job = getJob(jobId);
        return doMatch(student, job, null);
    }

    private MatchResultDTO doMatch(Student student, Job job, java.util.Map<String, Double> weights) {
        try {
            AIService.StudentProfile sp = objectMapper.readValue(student.getProfileJson(), AIService.StudentProfile.class);
            AIService.JobProfile jp = objectMapper.readValue(job.getProfileJson(), AIService.JobProfile.class);
            MatchResultDTO r = aiService.match(sp, jp, weights);
            log.info("匹配完成, studentId={}, jobId={}, totalScore={}", student.getId(), job.getId(), r.getTotalScore());
            return r;
        } catch (Exception e) {
            log.error("匹配失败", e);
            throw new BusinessException("匹配计算失败: " + e.getMessage());
        }
    }

    private Student getStudent(Long id) {
        Student s = studentMapper.selectById(id);
        if (s == null) throw new BusinessException("学生不存在, ID: " + id);
        if (s.getProfileJson() == null || s.getProfileJson().isEmpty()) throw new BusinessException("学生画像数据为空");
        return s;
    }

    private Job getJob(Long id) {
        Job j = jobMapper.selectById(id);
        if (j == null) throw new BusinessException("岗位不存在, ID: " + id);
        if (j.getProfileJson() == null || j.getProfileJson().isEmpty()) throw new BusinessException("岗位画像数据为空");
        return j;
    }
}
