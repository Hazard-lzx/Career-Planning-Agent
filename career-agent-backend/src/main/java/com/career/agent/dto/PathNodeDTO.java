package com.career.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 职业路径节点DTO
 */
@Data
@Schema(description = "职业路径节点")
public class PathNodeDTO {

    @Schema(description = "岗位ID")
    private Long jobId;

    @Schema(description = "职位编码")
    private String jobCode;

    @Schema(description = "职位名称")
    private String title;

    @Schema(description = "岗位级别")
    private String level;

    @Schema(description = "排序序号（垂直路径用）")
    private Integer order;

    @Schema(description = "相似度（水平路径用）")
    private Double similarity;

    @Schema(description = "换岗原因（水平路径用）")
    private String reason;
}
