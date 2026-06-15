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
 * 报告实体类
 * 对应数据库表：report
 */
@Data
@TableName("report")
@Schema(description = "报告实体")
public class Report {

    @TableId(type = IdType.AUTO)
    @Schema(description = "报告ID")
    private Long id;

    @Schema(description = "学生ID")
    private Long studentId;

    @Schema(description = "目标岗位ID")
    private Long targetJobId;

    @Schema(description = "报告全文")
    private String content;

    @Schema(description = "匹配结果快照JSON")
    private String matchResultJson;

    @Schema(description = "状态：draft/complete")
    private String status;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
