package com.career.agent.config;

/**
 * RocketMQ配置类
 * 定义消息主题常量，RocketMQTemplate由starter自动注入
 */
public class RocketMQConfig {

    /** 报告生成主题 */
    public static final String TOPIC_REPORT_GENERATE = "career-report-generate";

    /** 岗位图谱构建主题 */
    public static final String TOPIC_JOB_GRAPH_BUILD = "career-job-graph-build";

    /** 报告生成消费者组 */
    public static final String CONSUMER_GROUP_REPORT = "report-generate-consumer";

    /** 岗位图谱构建消费者组 */
    public static final String CONSUMER_GROUP_JOB_GRAPH = "job-graph-build-consumer";
}
