package com.career.agent.listener;

import com.career.agent.ai.AIService;
import com.career.agent.config.RocketMQConfig;
import com.career.agent.entity.Job;
import com.career.agent.mapper.JobMapper;
import com.career.agent.service.JobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 岗位图谱构建消息消费者 - 需要 RocketMQ 服务才能激活。
 * 当前 @Component 已注释，启动不会因 RocketMQ 不可用而崩溃。
 * 需要时取消注释 @Component 并确保 RocketMQ name-server 可连通。
 */
@Slf4j
// @Component  -- 取消注释以启用 RocketMQ 异步图谱构建
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = RocketMQConfig.TOPIC_JOB_GRAPH_BUILD,
        consumerGroup = RocketMQConfig.CONSUMER_GROUP_JOB_GRAPH
)
public class JobGraphBuildListener implements RocketMQListener<String> {

    private final JobMapper jobMapper;
    private final JobService jobService;
    private final AIService aiService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(String message) {
        log.info("收到图谱构建消息: {}", message);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = objectMapper.readValue(message, Map.class);
            @SuppressWarnings("unchecked")
            List<Number> ids = (List<Number>) msg.get("jobIds");
            if (ids == null || ids.isEmpty()) return;
            List<Long> jobIds = ids.stream().map(Number::longValue).toList();
            List<Job> jobs = new ArrayList<>();
            for (Long id : jobIds) {
                try { Job j = jobMapper.selectById(id); if (j != null) { jobService.generateJobProfile(j); jobs.add(j); } }
                catch (Exception e) { log.warn("画像生成失败 jobId={}", id, e); }
            }
            try { aiService.buildJobGraph(jobs); log.info("图谱构建完成"); }
            catch (Exception e) { log.warn("图谱构建失败", e); }
        } catch (Exception e) { log.error("图谱构建消息处理异常", e); }
    }
}
