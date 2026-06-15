package com.career.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.career.agent.ai.AIService;
import com.career.agent.common.BusinessException;
import com.career.agent.config.RocketMQConfig;
import com.career.agent.dto.JobQueryDTO;
import com.career.agent.dto.PageResultDTO;
import com.career.agent.dto.PathNodeDTO;
import com.career.agent.entity.Job;
import com.career.agent.mapper.JobMapper;
import com.career.agent.service.JobService;
import com.career.agent.service.PathService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 岗位服务实现类 (LangChain4j 版本)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobMapper jobMapper;
    private final AIService aiService;
    private final PathService pathService;
    private final ObjectMapper objectMapper;
    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public PageResultDTO<Job> listJobs(JobQueryDTO dto) {
        Page<Job> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Job> w = new LambdaQueryWrapper<>();
        if (dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty()) {
            String kw = dto.getKeyword().trim();
            w.and(q -> q.like(Job::getTitle, kw).or().like(Job::getCompanyFullName, kw).or().like(Job::getIndustry, kw));
        }
        w.orderByDesc(Job::getCreatedAt);
        Page<Job> r = jobMapper.selectPage(page, w);
        PageResultDTO<Job> pr = new PageResultDTO<>();
        pr.setRecords(r.getRecords()); pr.setTotal(r.getTotal());
        pr.setPage(dto.getPage()); pr.setSize(dto.getSize());
        pr.setPages((int) Math.ceil((double) r.getTotal() / dto.getSize()));
        return pr;
    }

    @Override
    public Job getJobDetail(Long id) {
        Job j = jobMapper.selectById(id);
        if (j == null) throw new BusinessException("岗位不存在, ID: " + id);
        return j;
    }

    @Override
    public int batchImport(MultipartFile file) {
        List<Job> jobs = parseCsv(file);
        if (jobs.isEmpty()) throw new BusinessException("CSV 无有效数据");
        int cnt = 0;
        for (Job j : jobs) { jobMapper.insert(j); cnt++; }
        log.info("CSV 导入 {} 条", cnt);
        List<Long> ids = jobs.stream().map(Job::getId).collect(Collectors.toList());
        Map<String, Object> msg = new HashMap<>(); msg.put("jobIds", ids);
        try {
            rocketMQTemplate.convertAndSend(RocketMQConfig.TOPIC_JOB_GRAPH_BUILD, objectMapper.writeValueAsString(msg));
        } catch (Exception e) {
            log.warn("MQ 发送失败, 同步处理", e);
            for (Job j : jobs) { try { generateJobProfile(j); } catch (Exception ex) { log.warn("生成失败 jobCode={}", j.getJobCode(), ex); } }
        }
        return cnt;
    }

    @Override
    public void generateJobProfile(Job job) {
        if (job.getDescription() == null || job.getDescription().trim().isEmpty()) return;
        AIService.JobProfile jp = aiService.parseJob(job.getDescription(), job.getTitle());
        try { job.setProfileJson(objectMapper.writeValueAsString(jp)); } catch (Exception e) { throw new RuntimeException(e); }
        jobMapper.updateById(job);
        log.info("岗位画像生成完成 jobCode={}", job.getJobCode());
    }

    @Override
    public Map<String, List<PathNodeDTO>> getCareerPaths(Long jobId) {
        Map<String, List<PathNodeDTO>> paths = new LinkedHashMap<>();
        paths.put("verticalPath", pathService.getVerticalPath(jobId));
        paths.put("horizontalPath", pathService.getHorizontalPath(jobId));
        return paths;
    }

    private List<Job> parseCsv(MultipartFile file) {
        try (InputStreamReader r = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser p = CSVParser.parse(r, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            List<Job> jobs = new ArrayList<>();
            for (CSVRecord rec : p) {
                Job j = new Job();
                j.setJobCode(csvVal(rec, "职位编码", "job_code"));
                j.setTitle(csvVal(rec, "职位名称", "title"));
                j.setCompanyFullName(csvVal(rec, "公司全称", "company_full_name"));
                j.setCompanyShortName(csvVal(rec, "公司简称", "company_short_name"));
                j.setIndustry(csvVal(rec, "所属行业", "industry"));
                j.setCompanySize(csvVal(rec, "人员规模", "company_size"));
                j.setCompanyType(csvVal(rec, "企业性质", "company_type"));
                j.setWorkLocation(csvVal(rec, "工作地址", "work_location"));
                j.setSalaryRange(csvVal(rec, "薪资范围", "salary_range"));
                j.setDescription(csvVal(rec, "职位描述", "description"));
                j.setCompanyProfile(csvVal(rec, "公司简介", "company_profile"));
                j.setLevel(csvVal(rec, "级别", "level"));
                jobs.add(j);
            }
            return jobs;
        } catch (Exception e) { throw new BusinessException("CSV 解析失败: " + e.getMessage()); }
    }

    private String csvVal(CSVRecord rec, String cn, String en) {
        try { String v = rec.get(cn); return (v != null && !v.trim().isEmpty()) ? v : rec.get(en); }
        catch (IllegalArgumentException e) { try { return rec.get(en); } catch (Exception ex) { return null; } }
    }
}
