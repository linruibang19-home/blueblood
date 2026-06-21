package com.blueblood.api.modules.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blueblood.api.modules.task.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    /**
     * 原子扣减剩余名额:仅当 slots_left>0 时 -1,并发安全(防超卖)。
     * 返回受影响行数:1=成功,0=名额已满。
     */
    @Update("UPDATE task SET slots_left = slots_left - 1 " +
            "WHERE id = #{taskId} AND slots_left > 0 AND deleted_at IS NULL")
    int decrementSlotsLeft(@Param("taskId") Long taskId);
}
