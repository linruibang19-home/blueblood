package com.blueblood.api.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.admin.dto.AdminRoleUserVO;
import com.blueblood.api.modules.admin.dto.AdminRoleVO;
import com.blueblood.api.modules.admin.dto.RoleAssignRequest;
import com.blueblood.api.modules.user.entity.SysRole;
import com.blueblood.api.modules.user.entity.SysUserRole;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.SysRoleMapper;
import com.blueblood.api.modules.user.mapper.SysUserRoleMapper;
import com.blueblood.api.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 后台权限管理：角色概览(含用户数)、角色用户列表、分配/撤销角色。
 */
@Service
@RequiredArgsConstructor
public class AdminRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final UserMapper userMapper;

    /** 所有角色 + 每个角色的用户数。 */
    public List<AdminRoleVO> listRoles() {
        List<SysRole> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .isNull(SysRole::getDeletedAt)
                .orderByAsc(SysRole::getId));

        if (roles.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> roleIds = roles.stream().map(SysRole::getId).collect(Collectors.toList());
        // 统计每个角色未删除的用户角色关联数
        List<SysUserRole> rels = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getRoleId, roleIds)
                .isNull(SysUserRole::getDeletedAt));
        Map<Long, Long> countMap = rels.stream()
                .collect(Collectors.groupingBy(SysUserRole::getRoleId, Collectors.counting()));

        return roles.stream().map(r -> toVO(r, countMap.getOrDefault(r.getId(), 0L)))
                .collect(Collectors.toList());
    }

    /** 某角色编码下的用户列表。 */
    public List<AdminRoleUserVO> listUsersByRole(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "角色编码不能为空");
        }
        SysRole role = getRoleByCode(roleCode);

        // 角色下的用户关联
        List<SysUserRole> rels = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, role.getId())
                .isNull(SysUserRole::getDeletedAt));
        if (rels.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = rels.stream().map(SysUserRole::getUserId).distinct()
                .collect(Collectors.toList());

        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .in(User::getId, userIds)
                .isNull(User::getDeletedAt));
        Map<Long, User> userMap = new HashMap<>(users.size() * 2);
        for (User u : users) {
            userMap.put(u.getId(), u);
        }

        List<AdminRoleUserVO> result = new ArrayList<>(userIds.size());
        for (Long uid : userIds) {
            User u = userMap.get(uid);
            if (u == null) {
                continue;
            }
            AdminRoleUserVO vo = new AdminRoleUserVO();
            vo.setUserId(u.getId());
            vo.setNickname(u.getNickname());
            vo.setUsername(u.getUsername());
            result.add(vo);
        }
        return result;
    }

    /** 给用户分配角色(已存在则提示)。 */
    @Transactional
    public void assign(RoleAssignRequest req) {
        SysRole role = getRoleByCode(req.getRoleCode());
        User user = userMapper.selectById(req.getUserId());
        if (user == null || user.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "用户不存在");
        }

        // 查是否已存在未删除的关联
        Long exists = sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, req.getUserId())
                .eq(SysUserRole::getRoleId, role.getId())
                .isNull(SysUserRole::getDeletedAt));
        if (exists != null && exists > 0) {
            throw new BusinessException(ResultCode.DATA_DUPLICATED, "该用户已拥有此角色");
        }

        SysUserRole rel = new SysUserRole();
        rel.setUserId(req.getUserId());
        rel.setRoleId(role.getId());
        sysUserRoleMapper.insert(rel);
    }

    /** 撤销用户角色(软删)。 */
    @Transactional
    public void revoke(RoleAssignRequest req) {
        SysRole role = getRoleByCode(req.getRoleCode());
        SysUserRole rel = sysUserRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, req.getUserId())
                .eq(SysUserRole::getRoleId, role.getId())
                .isNull(SysUserRole::getDeletedAt)
                .last("LIMIT 1"));
        if (rel == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "该用户未拥有此角色");
        }
        rel.setDeletedAt(LocalDateTime.now());
        sysUserRoleMapper.updateById(rel);
    }

    private SysRole getRoleByCode(String code) {
        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, code)
                .isNull(SysRole::getDeletedAt)
                .last("LIMIT 1"));
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "角色不存在: " + code);
        }
        return role;
    }

    private AdminRoleVO toVO(SysRole r, long userCount) {
        AdminRoleVO vo = new AdminRoleVO();
        vo.setId(r.getId());
        vo.setCode(r.getCode());
        vo.setName(r.getName());
        vo.setDescription(r.getDescription());
        vo.setStatus(r.getStatus());
        vo.setUserCount(userCount);
        return vo;
    }
}
