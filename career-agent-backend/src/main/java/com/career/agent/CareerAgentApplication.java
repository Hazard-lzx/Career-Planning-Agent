package com.career.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AI大学生职业规划智能体 - 业务后端启动类
 *
 * 技术栈：Spring Boot 3.3 + Spring AI + MyBatis-Plus + Neo4j + Redis
 * 访问API文档：http://localhost:8080/doc.html
 */
@SpringBootApplication
@MapperScan("com.career.agent.mapper")
@EnableAsync
public class CareerAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareerAgentApplication.class, args);
        System.out.println("============================================");
        System.out.println("  Career Agent 后端服务启动成功！");
        System.out.println("  API文档地址：http://localhost:8080/doc.html");
        System.out.println("============================================");
    }
}
