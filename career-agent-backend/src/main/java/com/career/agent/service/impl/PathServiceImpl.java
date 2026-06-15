package com.career.agent.service.impl;

import com.career.agent.common.BusinessException;
import com.career.agent.dto.PathNodeDTO;
import com.career.agent.entity.Job;
import com.career.agent.mapper.JobMapper;
import com.career.agent.neo4j.entity.JobNode;
import com.career.agent.neo4j.repository.JobNodeRepository;
import com.career.agent.service.PathService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 职业路径服务实现类
 * 从Neo4j知识图谱中查询职业发展路径
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PathServiceImpl implements PathService {

    private final JobNodeRepository jobNodeRepository;
    private final JobMapper jobMapper;

    @Override
    public List<PathNodeDTO> getVerticalPath(Long jobId) {
        // 1. 查询MySQL中的岗位信息，获取jobCode
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException("岗位不存在，ID：" + jobId);
        }

        // 2. 从Neo4j查询垂直晋升路径
        List<JobNode> pathNodes;
        try {
            pathNodes = jobNodeRepository.findVerticalPath(job.getJobCode());
        } catch (Exception e) {
            log.warn("Neo4j查询垂直路径失败：{}", e.getMessage());
            return new ArrayList<>();
        }

        // 3. 转换为DTO
        List<PathNodeDTO> result = new ArrayList<>();
        // 先加入当前岗位
        PathNodeDTO currentNode = new PathNodeDTO();
        currentNode.setJobId(jobId);
        currentNode.setJobCode(job.getJobCode());
        currentNode.setTitle(job.getTitle());
        currentNode.setLevel(job.getLevel());
        currentNode.setOrder(0);
        result.add(currentNode);

        // 添加后续晋升节点
        for (int i = 0; i < pathNodes.size(); i++) {
            JobNode node = pathNodes.get(i);
            PathNodeDTO dto = new PathNodeDTO();
            dto.setJobCode(node.getJobCode());
            dto.setTitle(node.getTitle());
            dto.setLevel(node.getLevel());
            dto.setOrder(i + 1);
            // 尝试从MySQL查找对应的jobId
            Job targetJob = findJobByCode(node.getJobCode());
            if (targetJob != null) {
                dto.setJobId(targetJob.getId());
            }
            result.add(dto);
        }

        return result;
    }

    @Override
    public List<PathNodeDTO> getHorizontalPath(Long jobId) {
        // 1. 查询MySQL中的岗位信息
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new BusinessException("岗位不存在，ID：" + jobId);
        }

        // 2. 从Neo4j查询水平换岗路径
        List<JobNode> pathNodes;
        try {
            pathNodes = jobNodeRepository.findHorizontalPath(job.getJobCode());
        } catch (Exception e) {
            log.warn("Neo4j查询水平路径失败：{}", e.getMessage());
            return new ArrayList<>();
        }

        // 3. 转换为DTO
        List<PathNodeDTO> result = new ArrayList<>();
        for (JobNode node : pathNodes) {
            PathNodeDTO dto = new PathNodeDTO();
            dto.setJobCode(node.getJobCode());
            dto.setTitle(node.getTitle());
            dto.setLevel(node.getLevel());
            // 尝试从MySQL查找对应的jobId
            Job targetJob = findJobByCode(node.getJobCode());
            if (targetJob != null) {
                dto.setJobId(targetJob.getId());
            }
            // 从关系中获取相似度和原因
            if (node.getHorizontalPaths() != null && !node.getHorizontalPaths().isEmpty()) {
                JobNode.HorizontalPath path = node.getHorizontalPaths().get(0);
                dto.setSimilarity(path.getSimilarity());
                dto.setReason(path.getReason());
            }
            result.add(dto);
        }

        return result;
    }

    /**
     * 根据jobCode从MySQL查询岗位
     */
    private Job findJobByCode(String jobCode) {
        try {
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Job> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(Job::getJobCode, jobCode);
            return jobMapper.selectOne(wrapper);
        } catch (Exception e) {
            return null;
        }
    }
}
