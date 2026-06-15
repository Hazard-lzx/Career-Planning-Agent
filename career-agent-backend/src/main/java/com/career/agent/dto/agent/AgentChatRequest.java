package com.career.agent.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Agent 会话请求 DTO
 */
@Data
@Schema(description = "Agent 会话请求")
public class AgentChatRequest {

    @Schema(description = "会话ID (用于记忆上下文)", example = "user-123")
    private String sessionId;

    @Schema(description = "用户消息", example = "我想去杭州做Java开发")
    private String message;
}
