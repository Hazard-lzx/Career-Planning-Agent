package com.career.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.career.agent.entity.Job;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位Mapper接口
 */
@Mapper
public interface JobMapper extends BaseMapper<Job> {
}
