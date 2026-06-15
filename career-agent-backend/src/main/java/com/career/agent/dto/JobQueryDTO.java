package com.career.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 岗位列表查询DTO
 */
@Data
@Schema(description = "岗位列表查询参数")
public class JobQueryDTO {

    @Schema(description = "当前页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "20")
    private Integer size = 20;

    @Schema(description = "搜索关键词")
    private String keyword;
}
