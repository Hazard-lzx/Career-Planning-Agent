package com.career.agent.controller;

import com.career.agent.agent.AgentService;
import com.career.agent.common.Result;
import com.career.agent.dto.agent.AgentChatRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Agent 会话控制器
 * 提供"一句话规划"智能导航入口 (POST /api/agent/chat)
 */
@RestController
@RequestMapping("/api/agent")
@Tag(name = "职业规划 Agent")
@CrossOrigin
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    /**
     * Agent 对话接口
     * 发送消息并获取 Agent 回复, sessionId 用于保持对话上下文。
     */
    @PostMapping("/chat")
    @Operation(summary = "Agent 智能对话", description = "发送消息给职业规划Agent, 自动调用工具完成规划任务")
    public Result<String> chat(@RequestBody AgentChatRequest request) {
        String reply = agentService.chat(
                request.getSessionId() != null ? request.getSessionId() : "default",
                request.getMessage());
        return Result.success(reply);
    }
}
