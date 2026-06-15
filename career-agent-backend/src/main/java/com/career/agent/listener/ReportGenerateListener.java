package com.career.agent.listener;

import com.career.agent.config.RocketMQConfig;
import com.career.agent.service.ReportService;
import com.career.agent.entity.Report;
import com.career.agent.mapper.ReportMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;

import java.util.Map;

/**
 * 报告生成消息消费者 - 需要 RocketMQ 服务才能激活。
 * 当前 @Component 已注释，启动不会因 RocketMQ 不可用而崩溃。
 * 需要时取消注释 @Component 并确保 RocketMQ name-server 可连通。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = RocketMQConfig.TOPIC_REPORT_GENERATE,
        consumerGroup = RocketMQConfig.CONSUMER_GROUP_REPORT
)
public class ReportGenerateListener implements RocketMQListener<String> {

    private final ReportService reportService;
    private final ReportMapper reportMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(String message) {
        log.info("收到报告生成消息: {}", message);
        try {
            Map<String, Object> msgMap = objectMapper.readValue(message, Map.class);
            Long studentId = toLong(msgMap.get("studentId"));
            Long jobId = toLong(msgMap.get("jobId"));
            Long reportId = toLong(msgMap.get("reportId"));
            if (studentId == null || jobId == null || reportId == null) {
                log.error("报告生成消息参数不完整: {}", message);
                return;
            }
            reportService.doGenerate(studentId, jobId, reportId);
        } catch (Exception e) {
            log.error("报告生成消费处理异常: {}", message, e);
            try {
                Map<String, Object> m = objectMapper.readValue(message, Map.class);
                Long rid = toLong(m.get("reportId"));
                if (rid != null) {
                    Report r = reportService.getReport(rid);
                    if (r != null) { r.setStatus("failed"); reportMapper.updateById(r); }
                }
            } catch (Exception ex) {}
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        try { return Long.parseLong(value.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}
