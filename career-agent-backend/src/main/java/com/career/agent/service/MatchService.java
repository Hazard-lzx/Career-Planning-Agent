package com.career.agent.service;

import com.career.agent.dto.MatchRequestDTO;
import com.career.agent.dto.MatchResultDTO;
/**
 * 人岗匹配服务接口
 */
public interface MatchService {

    /**
     * 计算人岗匹配
     */
    MatchResultDTO calculate(MatchRequestDTO requestDTO);

    /**
     * Agent 工具方法: 评估学生与指定岗位的匹配度。
     *
     * @param studentId 学生ID
     * @param jobId     岗位ID
     * @return 匹配结果 (总分、各维度得分、差距分析、匹配/缺失技能)
     */MatchResultDTO calculateMatch(Long studentId, Long jobId);
}
