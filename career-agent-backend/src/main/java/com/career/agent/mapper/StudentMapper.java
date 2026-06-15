package com.career.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.career.agent.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生Mapper接口
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
