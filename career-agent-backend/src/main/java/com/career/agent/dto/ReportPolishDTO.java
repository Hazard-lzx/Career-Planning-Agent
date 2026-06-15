package com.career.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 报告润色请求DTO
 */
@Data
@Schema(description = "报告润色请求")
public class ReportPolishDTO {

    @Schema(description = "章节名", required = true)
    private String section;

    @Schema(description = "待润色文本", required = true)
    private String text;
}
