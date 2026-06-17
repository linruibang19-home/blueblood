package com.blueblood.api.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blueblood.api.modules.system.entity.SysDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysDictMapper extends BaseMapper<SysDict> {

    /** 所有启用的字典类型(去重) */
    @Select("SELECT DISTINCT dict_type FROM sys_dict WHERE deleted_at IS NULL AND status='ACTIVE' ORDER BY dict_type")
    List<String> selectDistinctTypes();
}
