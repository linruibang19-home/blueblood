package com.blueblood.api.modules.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blueblood.api.modules.task.entity.TaskCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskCategoryMapper extends BaseMapper<TaskCategory> {
}
