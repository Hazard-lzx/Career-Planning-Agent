package com.career.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分页结果DTO
 * @param <T> 数据类型
 */
@Data
@Schema(description = "分页结果")
public class PageResultDTO<T> {

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "当前页码")
    private Integer page;

    @Schema(description = "每页条数")
    private Integer size;

    @Schema(description = "总页数")
    private Integer pages;
}
