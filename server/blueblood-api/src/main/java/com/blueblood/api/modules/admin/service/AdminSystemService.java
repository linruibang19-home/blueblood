package com.blueblood.api.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.admin.dto.*;
import com.blueblood.api.modules.system.entity.SysConfig;
import com.blueblood.api.modules.system.entity.SysDict;
import com.blueblood.api.modules.system.entity.SysOperationLog;
import com.blueblood.api.modules.system.mapper.SysConfigMapper;
import com.blueblood.api.modules.system.mapper.SysDictMapper;
import com.blueblood.api.modules.system.mapper.SysOperationLogMapper;
import com.blueblood.api.modules.task.entity.TaskCategory;
import com.blueblood.api.modules.task.mapper.TaskCategoryMapper;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.entity.UserSkill;
import com.blueblood.api.modules.user.mapper.UserMapper;
import com.blueblood.api.modules.user.mapper.UserSkillMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 后台系统配置与权限：
 * 系统配置 / 字典 / 任务分类 / 用户技能标签 / 操作日志 的增删改查。
 * 删除均为软删(set deletedAt=now)；查询均 .isNull(Entity::getDeletedAt)。
 * 操作日志不软删（仅查询展示）。
 */
@Service
@RequiredArgsConstructor
public class AdminSystemService {

    private final SysConfigMapper configMapper;
    private final SysDictMapper dictMapper;
    private final TaskCategoryMapper categoryMapper;
    private final UserSkillMapper skillMapper;
    private final SysOperationLogMapper logMapper;
    private final UserMapper userMapper;

    // ==================== 系统配置 ====================

    public PageResult<SysConfig> pageConfig(AdminSystemQuery query) {
        Page<SysConfig> page = query.toPage();
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<SysConfig>()
                .isNull(SysConfig::getDeletedAt)
                .orderByDesc(SysConfig::getCreatedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(SysConfig::getConfigKey, kw)
                    .or().like(SysConfig::getLabel, kw)
                    .or().like(SysConfig::getRemark, kw));
        }
        Page<SysConfig> result = configMapper.selectPage(page, wrapper);
        return PageResult.of(result);
    }

    @Transactional
    public Long createConfig(SysConfigRequest req) {
        if (!StringUtils.hasText(req.getConfigKey())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "配置键不能为空");
        }
        // 键唯一性校验
        Long exists = configMapper.selectCount(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, req.getConfigKey())
                .isNull(SysConfig::getDeletedAt));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "配置键已存在");
        }
        SysConfig c = new SysConfig();
        applyConfigFields(c, req);
        configMapper.insert(c);
        return c.getId();
    }

    @Transactional
    public void updateConfig(Long id, SysConfigRequest req) {
        SysConfig exists = getConfigEntity(id);
        // 若改键，需保证键唯一
        if (StringUtils.hasText(req.getConfigKey()) && !req.getConfigKey().equals(exists.getConfigKey())) {
            Long dup = configMapper.selectCount(new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getConfigKey, req.getConfigKey())
                    .ne(SysConfig::getId, id)
                    .isNull(SysConfig::getDeletedAt));
            if (dup != null && dup > 0) {
                throw new BusinessException(ResultCode.DATA_DUPLICATED, "配置键已存在");
            }
        }
        applyConfigFields(exists, req);
        configMapper.updateById(exists);
    }

    @Transactional
    public void deleteConfig(Long id) {
        SysConfig c = getConfigEntity(id);
        SysConfig patch = new SysConfig();
        patch.setId(c.getId());
        patch.setDeletedAt(LocalDateTime.now());
        configMapper.updateById(patch);
    }

    private void applyConfigFields(SysConfig c, SysConfigRequest req) {
        c.setConfigKey(req.getConfigKey());
        c.setConfigValue(req.getConfigValue());
        if (StringUtils.hasText(req.getConfigType())) {
            c.setConfigType(req.getConfigType());
        }
        c.setLabel(req.getLabel());
        c.setRemark(req.getRemark());
    }

    private SysConfig getConfigEntity(Long id) {
        SysConfig c = configMapper.selectById(id);
        if (c == null || c.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "配置不存在");
        }
        return c;
    }

    // ==================== 字典 ====================

    public PageResult<SysDict> pageDict(AdminSystemQuery query) {
        Page<SysDict> page = query.toPage();
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<SysDict>()
                .isNull(SysDict::getDeletedAt)
                .orderByAsc(SysDict::getDictType)
                .orderByAsc(SysDict::getSort);
        if (StringUtils.hasText(query.getDictType())) {
            wrapper.eq(SysDict::getDictType, query.getDictType());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(SysDict::getDictKey, kw)
                    .or().like(SysDict::getDictValue, kw)
                    .or().like(SysDict::getLabel, kw));
        }
        Page<SysDict> result = dictMapper.selectPage(page, wrapper);
        return PageResult.of(result);
    }

    public List<String> dictTypes() {
        return dictMapper.selectDistinctTypes();
    }

    @Transactional
    public Long createDict(SysDictRequest req) {
        SysDict d = new SysDict();
        applyDictFields(d, req);
        if (d.getStatus() == null) {
            d.setStatus("ACTIVE");
        }
        dictMapper.insert(d);
        return d.getId();
    }

    @Transactional
    public void updateDict(Long id, SysDictRequest req) {
        SysDict exists = getDictEntity(id);
        applyDictFields(exists, req);
        dictMapper.updateById(exists);
    }

    @Transactional
    public void deleteDict(Long id) {
        SysDict d = getDictEntity(id);
        SysDict patch = new SysDict();
        patch.setId(d.getId());
        patch.setDeletedAt(LocalDateTime.now());
        dictMapper.updateById(patch);
    }

    private void applyDictFields(SysDict d, SysDictRequest req) {
        d.setDictType(req.getDictType());
        d.setDictKey(req.getDictKey());
        d.setDictValue(req.getDictValue());
        d.setLabel(req.getLabel());
        if (req.getSort() != null) {
            d.setSort(req.getSort());
        }
        d.setRemark(req.getRemark());
        if (StringUtils.hasText(req.getStatus())) {
            d.setStatus(req.getStatus().toUpperCase());
        }
    }

    private SysDict getDictEntity(Long id) {
        SysDict d = dictMapper.selectById(id);
        if (d == null || d.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "字典项不存在");
        }
        return d;
    }

    // ==================== 任务分类 ====================

    public List<TaskCategory> listCategory() {
        return categoryMapper.selectList(new LambdaQueryWrapper<TaskCategory>()
                .isNull(TaskCategory::getDeletedAt)
                .orderByAsc(TaskCategory::getCategoryOrder));
    }

    @Transactional
    public Long createCategory(TaskCategoryRequest req) {
        TaskCategory c = new TaskCategory();
        applyCategoryFields(c, req);
        if (c.getTaskCount() == null) {
            c.setTaskCount(0);
        }
        if (c.getStatus() == null) {
            c.setStatus("ACTIVE");
        }
        categoryMapper.insert(c);
        return c.getId();
    }

    @Transactional
    public void updateCategory(Long id, TaskCategoryRequest req) {
        TaskCategory exists = getCategoryEntity(id);
        applyCategoryFields(exists, req);
        categoryMapper.updateById(exists);
    }

    @Transactional
    public void deleteCategory(Long id) {
        TaskCategory c = getCategoryEntity(id);
        TaskCategory patch = new TaskCategory();
        patch.setId(c.getId());
        patch.setDeletedAt(LocalDateTime.now());
        categoryMapper.updateById(patch);
    }

    private void applyCategoryFields(TaskCategory c, TaskCategoryRequest req) {
        c.setName(req.getName());
        c.setIcon(req.getIcon());
        if (req.getCategoryOrder() != null) {
            c.setCategoryOrder(req.getCategoryOrder());
        }
        if (StringUtils.hasText(req.getStatus())) {
            c.setStatus(req.getStatus().toUpperCase());
        }
    }

    private TaskCategory getCategoryEntity(Long id) {
        TaskCategory c = categoryMapper.selectById(id);
        if (c == null || c.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "分类不存在");
        }
        return c;
    }

    // ==================== 用户技能标签 ====================

    public PageResult<AdminSkillVO> pageSkill(AdminSystemQuery query) {
        Page<UserSkill> page = query.toPage();
        LambdaQueryWrapper<UserSkill> wrapper = new LambdaQueryWrapper<UserSkill>()
                .isNull(UserSkill::getDeletedAt)
                .orderByDesc(UserSkill::getCreatedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(UserSkill::getName, kw)
                    .or().like(UserSkill::getCategory, kw));
        }
        Page<UserSkill> result = skillMapper.selectPage(page, wrapper);

        // 批量补 username
        Map<Long, String> usernameMap = loadUsernames(result.getRecords());
        return PageResult.of(result.convert(s -> toSkillVO(s, usernameMap)));
    }

    @Transactional
    public void deleteSkill(Long id) {
        UserSkill s = skillMapper.selectById(id);
        if (s == null || s.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "技能不存在");
        }
        UserSkill patch = new UserSkill();
        patch.setId(s.getId());
        patch.setDeletedAt(LocalDateTime.now());
        skillMapper.updateById(patch);
    }

    private AdminSkillVO toSkillVO(UserSkill s, Map<Long, String> usernameMap) {
        AdminSkillVO vo = new AdminSkillVO();
        vo.setId(s.getId());
        vo.setUserId(s.getUserId());
        vo.setUsername(usernameMap.get(s.getUserId()));
        vo.setName(s.getName());
        vo.setCategory(s.getCategory());
        vo.setProficiency(s.getProficiency());
        vo.setCreatedAt(s.getCreatedAt());
        return vo;
    }

    private Map<Long, String> loadUsernames(List<UserSkill> skills) {
        Set<Long> ids = new HashSet<>();
        for (UserSkill s : skills) {
            if (s.getUserId() != null) {
                ids.add(s.getUserId());
            }
        }
        Map<Long, String> map = new HashMap<>();
        if (ids.isEmpty()) {
            return map;
        }
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .in(User::getId, ids)
                .isNull(User::getDeletedAt));
        for (User u : users) {
            map.put(u.getId(), u.getUsername());
        }
        return map;
    }

    // ==================== 操作日志 ====================

    public PageResult<SysOperationLog> pageLog(AdminSystemQuery query) {
        Page<SysOperationLog> page = query.toPage();
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<SysOperationLog>()
                .orderByDesc(SysOperationLog::getCreatedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(SysOperationLog::getUsername, kw)
                    .or().like(SysOperationLog::getAction, kw)
                    .or().like(SysOperationLog::getUrl, kw)
                    .or().like(SysOperationLog::getModule, kw));
        }
        if (StringUtils.hasText(query.getModule())) {
            wrapper.eq(SysOperationLog::getModule, query.getModule().toUpperCase());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(SysOperationLog::getStatus, query.getStatus().toUpperCase());
        }
        Page<SysOperationLog> result = logMapper.selectPage(page, wrapper);
        return PageResult.of(result);
    }
}
