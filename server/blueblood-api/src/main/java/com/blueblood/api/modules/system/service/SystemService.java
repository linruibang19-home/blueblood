package com.blueblood.api.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.system.entity.SysConfig;
import com.blueblood.api.modules.system.entity.SysDict;
import com.blueblood.api.modules.system.mapper.SysConfigMapper;
import com.blueblood.api.modules.system.mapper.SysDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统服务：数据字典、系统配置。
 */
@Service
@RequiredArgsConstructor
public class SystemService {

    private final SysDictMapper dictMapper;
    private final SysConfigMapper configMapper;

    // ============================== 字典 ==============================

    /** 字典类型列表(去重) */
    public List<String> dictTypes() {
        return dictMapper.selectDistinctTypes();
    }

    /** 某类型下的字典项 */
    public List<SysDict> dictItems(String dictType) {
        return dictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictType, dictType)
                .eq(SysDict::getStatus, "ACTIVE")
                .isNull(SysDict::getDeletedAt)
                .orderByAsc(SysDict::getSort));
    }

    // ============================== 系统配置 ==============================

    /** 读取配置值 */
    public String configValue(String key) {
        SysConfig c = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .isNull(SysConfig::getDeletedAt));
        return c == null ? null : c.getConfigValue();
    }

    /** 更新配置值（管理员） */
    public void updateConfig(String key, String value) {
        SysConfig c = configMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .isNull(SysConfig::getDeletedAt));
        if (c == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "配置项不存在");
        }
        c.setConfigValue(value);
        configMapper.updateById(c);
    }
}
