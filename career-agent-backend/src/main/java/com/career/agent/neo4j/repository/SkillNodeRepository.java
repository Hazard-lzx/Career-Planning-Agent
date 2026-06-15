package com.career.agent.neo4j.repository;

import com.career.agent.neo4j.entity.SkillNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

/**
 * 技能节点Repository
 */
@Repository
public interface SkillNodeRepository extends Neo4jRepository<SkillNode, String> {
}
