package com.career.agent.controller;

import com.career.agent.common.Result;
import com.career.agent.entity.Student;
import com.career.agent.service.StudentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 学生简历管理控制器
 * 接口前缀：/api/student
 */
@RestController
@RequestMapping("/api/student")
@Tag(name = "学生简历管理")
@CrossOrigin
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * 上传Word简历，用POI提取文本，调Python解析，返回画像
     */
    @PostMapping("/resume/upload")
    @Operation(summary = "上传简历并解析生成能力画像")
    public Result<Student> uploadResume(
            @Parameter(description = "Word/PDF简历文件", required = true)
            @RequestParam("file") MultipartFile file) {
        Student student = studentService.uploadResumeAndParse(file);
        return Result.success(student);
    }

    /**
     * 获取学生画像（含profile_json）
     */
    @GetMapping("/profile/{id}")
    @Operation(summary = "获取学生能力画像")
    public Result<Student> getProfile(
            @Parameter(description = "学生ID", required = true)
            @PathVariable Long id) {
        Student student = studentService.getProfile(id);
        return Result.success(student);
    }

    /**
     * 更新画像JSON
     */
    @PutMapping("/profile/{id}")
    @Operation(summary = "更新学生能力画像")
    public Result<Student> updateProfile(
            @Parameter(description = "学生ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "画像JSON字符串", required = true)
            @RequestBody String profileJson) {
        Student student = studentService.updateProfile(id, profileJson);
        return Result.success(student);
    }
}
