package com.career.agent.neo4j.entity;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Neo4j岗位节点实体
 * 用于构建职业发展知识图谱
 */
@Data
@Node("Job")
public class JobNode {

    @Id
    @Property("jobCode")
    private String jobCode;

    @Property("title")
    private String title;

    @Property("level")
    private String level;

    @Property("industry")
    private String industry;

    /**
     * 垂直晋升路径关系
     * direction = OUTGOING 表示当前岗位可以晋升到目标岗位
     */
    @Relationship(type = "VERTICAL_PATH", direction = Relationship.Direction.OUTGOING)
    private List<VerticalPath> verticalPaths = new ArrayList<>();

    /**
     * 水平换岗路径关系
     * direction = OUTGOING 表示当前岗位可以换到目标岗位
     */
    @Relationship(type = "HORIZONTAL_PATH", direction = Relationship.Direction.OUTGOING)
    private List<HorizontalPath> horizontalPaths = new ArrayList<>();

    /**
     * 垂直晋升路径关系实体
     */
    @Data
    public static class VerticalPath {
        @Property("order")
        private Integer order;

        private JobNode targetJob;
    }

    /**
     * 水平换岗路径关系实体
     */
    @Data
    public static class HorizontalPath {
        @Property("similarity")
        private Double similarity;

        @Property("reason")
        private String reason;

        private JobNode targetJob;
    }
}
