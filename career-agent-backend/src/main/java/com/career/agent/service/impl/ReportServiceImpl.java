package com.career.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.career.agent.ai.AIService;
import com.career.agent.common.BusinessException;
import com.career.agent.config.RocketMQConfig;
import com.career.agent.dto.MatchResultDTO;
import com.career.agent.dto.PathNodeDTO;
import com.career.agent.dto.ReportPolishDTO;
import com.career.agent.entity.Job;
import com.career.agent.entity.Report;
import com.career.agent.entity.Student;
import com.career.agent.mapper.JobMapper;
import com.career.agent.mapper.ReportMapper;
import com.career.agent.mapper.StudentMapper;
import com.career.agent.service.MatchService;
import com.career.agent.service.PathService;
import com.career.agent.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final StudentMapper studentMapper;
    private final JobMapper jobMapper;
    private final MatchService matchService;
    private final PathService pathService;
    private final AIService aiService;
    private final ObjectMapper objectMapper;
    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public Report generate(Long studentId, Long jobId) {
        Student s = studentMapper.selectById(studentId);
        if (s == null) throw new BusinessException("学生不存在");
        Job j = jobMapper.selectById(jobId);
        if (j == null) throw new BusinessException("岗位不存在");
        Report r = new Report();
        r.setStudentId(studentId); r.setTargetJobId(jobId);
        r.setContent(""); r.setMatchResultJson("{}"); r.setStatus("pending");
        reportMapper.insert(r);
        Map<String, Object> msg = Map.of("studentId", studentId, "jobId", jobId, "reportId", r.getId());
        try { rocketMQTemplate.convertAndSend(RocketMQConfig.TOPIC_REPORT_GENERATE, objectMapper.writeValueAsString(msg)); }
        catch (Exception e) { log.warn("MQ发送失败: {}", e.getMessage()); }
        return r;
    }

    @Override
    public Report generateReport(Long studentId, Long jobId) {
        Report r = generate(studentId, jobId);
        doGenerate(studentId, jobId, r.getId());
        return reportMapper.selectById(r.getId());
    }

    @Override
    public void doGenerate(Long studentId, Long jobId, Long reportId) {
        Report r = reportMapper.selectById(reportId);
        if (r == null) return;
        try {
            Student s = studentMapper.selectById(studentId);
            Job j = jobMapper.selectById(jobId);
            MatchResultDTO mr = matchService.calculateMatch(studentId, jobId);
            List<PathNodeDTO> vp = pathService.getVerticalPath(jobId);
            List<PathNodeDTO> hp = pathService.getHorizontalPath(jobId);
            String draft = buildDraft(s, j, mr, vp, hp);
            String polished;
            try { polished = aiService.polish(draft, "full_report"); }
            catch (Exception e) { polished = draft; }
            r.setContent(polished);
            r.setMatchResultJson(objectMapper.writeValueAsString(mr));
            r.setStatus("complete"); r.setUpdatedAt(LocalDateTime.now());
            reportMapper.updateById(r);
            log.info("报告生成完成 reportId={}", reportId);
        } catch (Exception e) {
            log.error("报告生成失败 reportId={}", reportId, e);
            r.setStatus("failed"); r.setContent("生成失败: " + e.getMessage());
            r.setUpdatedAt(LocalDateTime.now()); reportMapper.updateById(r);
        }
    }

    @Override
    public Report getReport(Long id) {
        Report r = reportMapper.selectById(id);
        if (r == null) throw new BusinessException("报告不存在");
        return r;
    }

    @Override public List<Report> listByStudentId(Long sid) {
        LambdaQueryWrapper<Report> w = new LambdaQueryWrapper<>();
        w.eq(Report::getStudentId, sid).orderByDesc(Report::getCreatedAt);
        return reportMapper.selectList(w);
    }

    @Override public Report updateContent(Long id, String content) {
        Report r = reportMapper.selectById(id);
        if (r == null) throw new BusinessException("报告不存在");
        r.setContent(content); reportMapper.updateById(r);
        return r;
    }

    @Override public String polishSection(Long id, ReportPolishDTO dto) {
        return aiService.polish(dto.getText(), dto.getSection());
    }

    @Override public byte[] exportWord(Long id) {
        Report r = getReport(id);
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            String[] chapters = r.getContent().split("(?=第[一二三四五六]章)");
            for (String ch : chapters) {
                if (ch.trim().isEmpty()) continue;
                for (String line : ch.split("\n")) {
                    line = line.trim(); if (line.isEmpty()) continue;
                    XWPFParagraph p = doc.createParagraph(); XWPFRun run = p.createRun();
                    if (line.startsWith("第") && line.contains("章")) { run.setBold(true); run.setFontSize(16); }
                    else if (line.startsWith("##")) { run.setBold(true); run.setFontSize(14); line = line.replaceAll("^#+\\s*", ""); }
                    else { run.setFontSize(12); }
                    if (line.contains("雷达图")) { line = "[雷达图 - 请参考在线展示]"; run.setColor("FF0000"); }
                    run.setText(line); run.setFontFamily("宋体");
                }
                doc.createParagraph();
            }
            doc.write(out); return out.toByteArray();
        } catch (Exception e) { throw new BusinessException("Word导出失败: " + e.getMessage()); }
    }

    @Override public void deleteReport(Long id) { reportMapper.deleteById(id); }

    private String buildDraft(Student s, Job j, MatchResultDTO mr, List<PathNodeDTO> vp, List<PathNodeDTO> hp) {
        StringBuilder sb = new StringBuilder();
        sb.append("第一章 自我认知\n\n");
        if (s.getProfileJson() != null) {
            try {
                @SuppressWarnings("unchecked") Map<String,Object> pf = objectMapper.readValue(s.getProfileJson(), Map.class);
                @SuppressWarnings("unchecked") Map<String,Object> edu = (Map<String,Object>) pf.get("education");
                if (edu != null) sb.append("## 教育背景\n学历：").append(edu.getOrDefault("degree","")).append("\n专业：").append(edu.getOrDefault("major","")).append("\n学校：").append(edu.getOrDefault("school","")).append("\n\n");
                @SuppressWarnings("unchecked") List<String> sk = (List<String>) pf.get("skills");
                if (sk != null && !sk.isEmpty()) sb.append("## 专业技能\n").append(String.join("、", sk)).append("\n\n");
            } catch (Exception ignored) {}
        }
        sb.append("第二章 目标岗位剖析\n\n岗位：").append(j.getTitle()).append("\n公司：").append(j.getCompanyFullName()).append("\n行业：").append(j.getIndustry()).append("\n薪资：").append(j.getSalaryRange()).append("\n地点：").append(j.getWorkLocation()).append("\n\n");
        sb.append("第三章 人岗匹配分析\n\n匹配总分：").append(String.format("%.2f", mr.getTotalScore()*100)).append("分\n[雷达图]\n");
        sb.append(String.format("- 基本条件: %.2f%%\n- 硬技能: %.2f%%\n- 软技能: %.2f%%\n- 潜力: %.2f%%\n\n", 
                mr.getDimensions().getBasic()*100, mr.getDimensions().getHardSkill()*100, mr.getDimensions().getSoftSkill()*100, mr.getDimensions().getPotential()*100));
        sb.append("差距分析: ").append(mr.getGapAnalysis()).append("\n\n");
        sb.append("第四章 职业发展路径\n\n垂直晋升:\n");
        if (vp != null) vp.forEach(n -> sb.append("→").append(n.getTitle()).append("(").append(n.getLevel()).append(")\n"));
        sb.append("\n水平换岗:\n");
        if (hp != null) hp.forEach(n -> sb.append("→").append(n.getTitle()).append(n.getSimilarity()!=null?String.format("(%.0f%%)",n.getSimilarity()*100):"").append("\n"));
        sb.append("\n第五章 分阶段行动计划\n\n");
        if (mr.getMissingSkills() != null) mr.getMissingSkills().forEach(sk -> sb.append("- 学习 ").append(sk).append("\n"));
        sb.append("\n第六章 评估指标与调整建议\n\n每半年自我评估, 关注行业趋势持续学习。\n");
        return sb.toString();
    }
}
