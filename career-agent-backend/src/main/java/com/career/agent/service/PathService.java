package com.career.agent.service;

import com.career.agent.dto.PathNodeDTO;

import java.util.List;

/**
 * 职业路径服务接口
 */
public interface PathService {

    /**
     * 获取垂直晋升路径
     * @param jobId 岗位ID
     * @return 垂直晋升节点列表
     */
    List<PathNodeDTO> getVerticalPath(Long jobId);

    /**
     * 获取水平换岗路径
     * @param jobId 岗位ID
     * @return 换岗路径列表（至少2条）
     */
    List<PathNodeDTO> getHorizontalPath(Long jobId);
}
