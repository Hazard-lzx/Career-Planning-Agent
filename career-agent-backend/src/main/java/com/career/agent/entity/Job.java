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
 * 岗位实体类
 * 对应数据库表：job
 */
@Data
@TableName("job")
@Schema(description = "岗位实体")
public class Job {

    @TableId(type = IdType.AUTO)
    @Schema(description = "自增主键")
    private Long id;

    @Schema(description = "职位编码（原始ID）")
    private String jobCode;

    @Schema(description = "职位名称")
    private String title;

    @Schema(description = "公司全称")
    private String companyFullName;

    @Schema(description = "公司简称")
    private String companyShortName;

    @Schema(description = "所属行业")
    private String industry;

    @Schema(description = "人员规模")
    private String companySize;

    @Schema(description = "企业性质")
    private String companyType;

    @Schema(description = "工作地址")
    private String workLocation;

    @Schema(description = "薪资范围")
    private String salaryRange;

    @Schema(description = "职位描述（原始文本）")
    private String description;

    @Schema(description = "公司简介")
    private String companyProfile;

    @Schema(description = "AI生成的岗位要求画像JSON")
    private String profileJson;

    @Schema(description = "岗位级别：初级/中级/高级")
    private String level;

    @Schema(description = "创建时间")
    @TableField("created_at")
    private LocalDateTime createdAt;
}
