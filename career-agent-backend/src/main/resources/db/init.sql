-- ============================================
-- AI大学生职业规划智能体 - 数据库初始化脚本
-- 数据库：career_agent（编码：utf8mb4）
-- ============================================

CREATE DATABASE IF NOT EXISTS career_agent DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE career_agent;

-- ----------------------------
-- 学生表
-- ----------------------------
DROP TABLE IF EXISTS `report`;
DROP TABLE IF EXISTS `student`;
DROP TABLE IF EXISTS `job`;

CREATE TABLE `student` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '学生ID',
    `name` VARCHAR(100) NOT NULL COMMENT '姓名',
    `email` VARCHAR(200) UNIQUE COMMENT '邮箱',
    `raw_resume_text` TEXT COMMENT '原始简历文本',
    `profile_json` TEXT COMMENT '学生能力画像JSON',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学生表';

-- ----------------------------
-- 岗位表
-- ----------------------------
CREATE TABLE `job` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `job_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '职位编码（原始ID）',
    `title` VARCHAR(200) NOT NULL COMMENT '职位名称',
    `company_full_name` VARCHAR(200) COMMENT '公司全称',
    `company_short_name` VARCHAR(100) COMMENT '公司简称',
    `industry` VARCHAR(100) COMMENT '所属行业',
    `company_size` VARCHAR(50) COMMENT '人员规模',
    `company_type` VARCHAR(50) COMMENT '企业性质',
    `work_location` VARCHAR(200) COMMENT '工作地址',
    `salary_range` VARCHAR(100) COMMENT '薪资范围',
    `description` TEXT COMMENT '职位描述（原始文本）',
    `company_profile` TEXT COMMENT '公司简介',
    `profile_json` TEXT COMMENT 'AI生成的岗位要求画像JSON',
    `level` VARCHAR(20) COMMENT '岗位级别：初级/中级/高级',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_job_code` (`job_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='岗位表';

-- ----------------------------
-- 报告表
-- ----------------------------
CREATE TABLE `report` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报告ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `target_job_id` BIGINT NOT NULL COMMENT '目标岗位ID',
    `content` TEXT COMMENT '报告全文',
    `match_result_json` TEXT COMMENT '匹配结果快照JSON',
    `status` VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/complete',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_target_job_id` (`target_job_id`),
    CONSTRAINT `fk_report_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_report_job` FOREIGN KEY (`target_job_id`) REFERENCES `job` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='报告表';
