package com.career.agent.service;

import com.career.agent.dto.ReportPolishDTO;
import com.career.agent.entity.Report;
import java.util.List;

/**
 * 报告服务接口
 */
public interface ReportService {

    /**
     * 创建报告记录并发送异步生成消息
     */
    Report generate(Long studentId, Long jobId);

    /**
     * 异步执行报告生成逻辑（由消费者调用）
     */
    void doGenerate(Long studentId, Long jobId, Long reportId);

    /**
     * 获取报告
     */
    Report getReport(Long id);

    /**
     * 按学生ID查询报告列表
     */
    List<Report> listByStudentId(Long studentId);

    /**
     * 更新报告内容
     */
    Report updateContent(Long id, String content);

    /**
     * 润色报告单段内容
     */
    String polishSection(Long id, ReportPolishDTO polishDTO);

    /**
     * 导出报告为Word文档
     */
    byte[] exportWord(Long id);

    /**
     * 删除报告
     */
    void deleteReport(Long id);

    /**
     * Agent 工具方法: 生成职业规划报告 (同步, 供 Agent 直接调用)。
     *
     * @param studentId 学生ID
     * @param jobId     目标岗位ID
     * @return 生成的报告实体 (含完整内容和匹配结果)
     */Report generateReport(Long studentId, Long jobId);
}
