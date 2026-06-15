package com.career.agent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生实体类
 * 对应数据库表：student
 */
@Data
@TableName("student")
@Schema(description = "学生实体")
public class Student {

    @TableId(type = IdType.AUTO)
    @Schema(description = "学生ID")
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "原始简历文本")
    private String rawResumeText;

    @Schema(description = "学生能力画像JSON")
    private String profileJson;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
