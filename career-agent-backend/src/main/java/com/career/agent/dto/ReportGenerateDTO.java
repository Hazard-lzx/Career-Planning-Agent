package com.career.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 报告生成请求DTO
 */
@Data
@Schema(description = "报告生成请求")
public class ReportGenerateDTO {

    @Schema(description = "学生ID", required = true)
    private Long studentId;

    @Schema(description = "岗位ID", required = true)
    private Long jobId;
}
