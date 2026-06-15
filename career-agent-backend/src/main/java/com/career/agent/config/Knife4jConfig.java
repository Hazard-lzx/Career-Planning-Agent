package com.career.agent.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / Springdoc OpenAPI 配置 (Spring Boot 3.x 兼容)
 * 访问路径：/doc.html
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI careerAgentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI大学生职业规划智能体接口文档")
                        .description("基于Spring AI的大学生职业规划智能体系后端接口文档，" +
                                "包含学生简历管理、岗位管理、人岗匹配、报告管理、职业路径等模块")
                        .version("2.0")
                        .contact(new Contact().name("Career Agent Team")));
    }
}
