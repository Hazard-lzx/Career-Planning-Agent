package com.career.agent.service.impl;

import com.career.agent.ai.AIService;
import com.career.agent.common.BusinessException;
import com.career.agent.entity.Student;
import com.career.agent.mapper.StudentMapper;
import com.career.agent.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * 学生服务实现类 (LangChain4j 版本)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;
    private final AIService aiService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_PREFIX = "student:profile:";

    @Override
    public Student uploadResumeAndParse(MultipartFile file) {
        String rawText = extractText(file);
        Student student = new Student();
        student.setRawResumeText(rawText);
        student.setName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "待完善");
        studentMapper.insert(student);

        // 使用 AIService 解析简历
        AIService.StudentProfile profile = aiService.parseResume(rawText);
        String profileJson = toJson(profile);
        student.setProfileJson(profileJson);
        studentMapper.updateById(student);
        cache(student.getId(), profileJson);
        log.info("简历解析完成, 学生ID={}", student.getId());
        return student;
    }

    @Override
    public Student getProfile(Long id) {
        String cached = redisTemplate.opsForValue().get(CACHE_PREFIX + id);
        Student s = studentMapper.selectById(id);
        if (s == null) throw new BusinessException("学生不存在, ID: " + id);
        if (cached != null) s.setProfileJson(cached);
        return s;
    }

    @Override
    public Student updateProfile(Long id, String profileJson) {
        Student s = studentMapper.selectById(id);
        if (s == null) throw new BusinessException("学生不存在, ID: " + id);
        s.setProfileJson(profileJson);
        studentMapper.updateById(s);
        cache(id, profileJson);
        log.info("学生画像更新, ID={}", id);
        return s;
    }

    private String extractText(MultipartFile file) {
        try (InputStream is = file.getInputStream(); XWPFDocument doc = new XWPFDocument(is)) {
            StringBuilder sb = new StringBuilder();
            for (XWPFParagraph p : doc.getParagraphs()) {
                String t = p.getText();
                if (t != null && !t.trim().isEmpty()) sb.append(t).append("\n");
            }
            for (var tbl : doc.getTables())
                for (var row : tbl.getRows())
                    for (var cell : row.getTableCells()) {
                        String ct = cell.getText();
                        if (ct != null && !ct.trim().isEmpty()) sb.append(ct).append("\n");
                    }
            String r = sb.toString().trim();
            if (r.isEmpty()) throw new BusinessException("简历内容为空");
            return r;
        } catch (BusinessException e) { throw e; }
        catch (Exception e) { throw new BusinessException("解析Word失败: " + e.getMessage()); }
    }

    private void cache(Long id, String json) {
        try { redisTemplate.opsForValue().set(CACHE_PREFIX + id, json, 1, TimeUnit.HOURS); }
        catch (Exception e) { log.warn("Redis缓存写入失败", e); }
    }

    private String toJson(Object obj) {
        try { return objectMapper.writeValueAsString(obj); }
        catch (Exception e) { throw new RuntimeException("序列化失败", e); }
    }
}
