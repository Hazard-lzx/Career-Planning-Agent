// ============================================
// AI大学生职业规划智能体 - Neo4j初始化脚本
// 用途：创建约束和索引，确保知识图谱数据完整性
// ============================================

// 创建岗位节点约束（确保jobCode唯一）
CREATE CONSTRAINT job_code_unique IF NOT EXISTS
FOR (j:Job) REQUIRE j.jobCode IS UNIQUE;

// 创建岗位节点索引（按级别查询）
CREATE INDEX job_level_index IF NOT EXISTS
FOR (j:Job) ON (j.level);

// 创建岗位节点索引（按行业查询）
CREATE INDEX job_industry_index IF NOT EXISTS
FOR (j:Job) ON (j.industry);

// 创建技能节点约束（确保技能名唯一）
CREATE CONSTRAINT skill_name_unique IF NOT EXISTS
FOR (s:Skill) REQUIRE s.name IS UNIQUE;

// 创建技能节点索引
CREATE INDEX skill_name_index IF NOT EXISTS
FOR (s:Skill) ON (s.name);

// ============================================
// 说明：
// Job节点属性：jobCode, title, level, industry
// Skill节点属性：name, category
// REQUIRES_SKILL关系：Job -[:REQUIRES_SKILL]-> Skill
// VERTICAL_PATH关系：Job -[:VERTICAL_PATH {order: int}]-> Job（垂直晋升）
// HORIZONTAL_PATH关系：Job -[:HORIZONTAL_PATH {similarity: float, reason: string}]-> Job（水平换岗）
// ============================================

// 示例数据（可选，用于测试）
// MERGE (j1:Job {jobCode: 'J001', title: '初级Java开发工程师', level: '初级', industry: '互联网'})
// MERGE (j2:Job {jobCode: 'J002', title: '中级Java开发工程师', level: '中级', industry: '互联网'})
// MERGE (j3:Job {jobCode: 'J003', title: '高级Java开发工程师', level: '高级', industry: '互联网'})
// MERGE (j1)-[:VERTICAL_PATH {order: 1}]->(j2)
// MERGE (j2)-[:VERTICAL_PATH {order: 2}]->(j3)
