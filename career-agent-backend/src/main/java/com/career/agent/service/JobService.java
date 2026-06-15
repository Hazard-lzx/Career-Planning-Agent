package com.career.agent.service;

import com.career.agent.dto.JobQueryDTO;
import com.career.agent.dto.PageResultDTO;
import com.career.agent.dto.PathNodeDTO;
import com.career.agent.entity.Job;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 岗位服务接口
 */
public interface JobService {

    /**
     * 分页查询岗位列表
     */
    PageResultDTO<Job> listJobs(JobQueryDTO queryDTO);

    /**
     * 获取岗位详情
     */
    Job getJobDetail(Long id);

    /**
     * 批量导入岗位数据（CSV文件）
     */
    int batchImport(MultipartFile file);

    /**
     * 为单个岗位生成画像并更新
     */
    void generateJobProfile(Job job);

    /**
     * Agent 工具方法: 获取岗位的垂直晋升和水平换岗路径。
     *
     * @param jobId 岗位ID
     * @return 包含垂直路径和水平路径的 Map:
     *         "verticalPath" -> List<PathNodeDTO>,
     *         "horizontalPath" -> List<PathNodeDTO>
     */java.util.Map<String, List<PathNodeDTO>> getCareerPaths(Long jobId);
}
