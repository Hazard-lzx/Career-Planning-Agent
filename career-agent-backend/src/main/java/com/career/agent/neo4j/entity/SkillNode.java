package com.career.agent.neo4j.entity;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import lombok.Data;

/**
 * Neo4j技能节点实体
 */
@Data
@Node("Skill")
public class SkillNode {

    @Id
    @Property("name")
    private String name;

    @Property("category")
    private String category;
}
