package com.career.agent.service;

import com.career.agent.entity.Student;
import org.springframework.web.multipart.MultipartFile;

/**
 * 学生服务接口
 */
public interface StudentService {

    /**
     * 上传简历并解析生成能力画像
     */
    Student uploadResumeAndParse(MultipartFile file);

    /**
     * 获取学生能力画像
     */Student getProfile(Long studentId);

    /**
     * 更新学生画像JSON
     */
    Student updateProfile(Long id, String profileJson);
}
