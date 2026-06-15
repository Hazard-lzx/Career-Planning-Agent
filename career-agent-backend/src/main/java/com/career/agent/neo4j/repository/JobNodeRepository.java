package com.career.agent.neo4j.repository;

import com.career.agent.neo4j.entity.JobNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 岗位知识图谱Repository
 * 提供Neo4j图数据库的查询操作
 */
@Repository
public interface JobNodeRepository extends Neo4jRepository<JobNode, String> {

    /**
     * 根据jobCode查找岗位节点
     */
    Optional<JobNode> findByJobCode(String jobCode);

    /**
     * 查询垂直晋升路径
     * 从当前岗位出发，沿VERTICAL_PATH关系查找所有上级岗位，按order排序
     */
    @Query("MATCH (start:Job {jobCode: $jobCode})-[:VERTICAL_PATH*1..5]->(target:Job) " +
           "MATCH (start)-[r:VERTICAL_PATH]->(next:Job) " +
           "WITH start, next, r " +
           "ORDER BY r.order " +
           "RETURN next")
    List<JobNode> findVerticalPath(String jobCode);

    /**
     * 查询水平换岗路径
     * 从当前岗位出发，沿HORIZONTAL_PATH关系查找可换岗的岗位
     */
    @Query("MATCH (start:Job {jobCode: $jobCode})-[r:HORIZONTAL_PATH]->(target:Job) " +
           "RETURN target, r.similarity AS similarity, r.reason AS reason " +
           "ORDER BY r.similarity DESC")
    List<JobNode> findHorizontalPath(String jobCode);

    /**
     * 删除所有岗位节点（用于重建图谱）
     */
    @Query("MATCH (n:Job) DETACH DELETE n")
    void deleteAllJobNodes();
}
