package com.blueblood.api.modules.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.PageResult;
import com.blueblood.api.common.result.ResultCode;
import com.blueblood.api.modules.admin.dto.AdminHackathonQuery;
import com.blueblood.api.modules.admin.dto.AdminHackathonVO;
import com.blueblood.api.modules.enterprise.dto.HackathonRequest;
import com.blueblood.api.modules.enterprise.entity.Hackathon;
import com.blueblood.api.modules.enterprise.mapper.HackathonMapper;
import com.blueblood.api.modules.user.entity.User;
import com.blueblood.api.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台黑客松管理：列表(筛选分页，含发布人昵称)、新增/编辑/删除。
 */
@Service
@RequiredArgsConstructor
public class AdminHackathonService {

    private final HackathonMapper hackathonMapper;
    private final UserMapper userMapper;

    public PageResult<AdminHackathonVO> page(AdminHackathonQuery query) {
        Page<Hackathon> page = query.toPage();
        LambdaQueryWrapper<Hackathon> wrapper = new LambdaQueryWrapper<Hackathon>()
                .isNull(Hackathon::getDeletedAt)
                .orderByDesc(Hackathon::getCreatedAt);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(Hackathon::getTitle, kw).or().like(Hackathon::getLocation, kw));
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Hackathon::getStatus, query.getStatus());
        }
        Page<Hackathon> result = hackathonMapper.selectPage(page, wrapper);
        Map<Long, String> publisherMap = loadNicknames(result.getRecords());
        return PageResult.of(result.convert(h -> toVO(h, publisherMap.get(h.getPublishedBy()))));
    }

    public AdminHackathonVO detail(Long id) {
        Hackathon hackathon = getHackathon(id);
        User u = hackathon.getPublishedBy() == null ? null : userMapper.selectById(hackathon.getPublishedBy());
        return toVO(hackathon, u == null ? null : u.getNickname());
    }

    @Transactional
    public Long create(HackathonRequest req) {
        Hackathon hackathon = new Hackathon();
        applyFields(hackathon, req);
        hackathon.setStatus(StringUtils.hasText(req.getStatus()) ? req.getStatus() : "signup");
        hackathon.setCurrentTeams(req.getCurrentTeams() == null ? 0 : req.getCurrentTeams());
        hackathonMapper.insert(hackathon);
        return hackathon.getId();
    }

    @Transactional
    public void update(Long id, HackathonRequest req) {
        Hackathon hackathon = getHackathon(id);
        applyFields(hackathon, req);
        if (StringUtils.hasText(req.getStatus())) {
            hackathon.setStatus(req.getStatus());
        }
        if (req.getCurrentTeams() != null) {
            hackathon.setCurrentTeams(req.getCurrentTeams());
        }
        hackathonMapper.updateById(hackathon);
    }

    @Transactional
    public void delete(Long id) {
        Hackathon hackathon = getHackathon(id);
        Hackathon patch = new Hackathon();
        patch.setId(hackathon.getId());
        patch.setDeletedAt(LocalDateTime.now());
        hackathonMapper.updateById(patch);
    }

    private Hackathon getHackathon(Long id) {
        Hackathon hackathon = hackathonMapper.selectById(id);
        if (hackathon == null || hackathon.getDeletedAt() != null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "黑客松不存在");
        }
        return hackathon;
    }

    private void applyFields(Hackathon hackathon, HackathonRequest req) {
        hackathon.setTitle(req.getTitle());
        hackathon.setDescription(req.getDescription() == null ? "" : req.getDescription());
        hackathon.setCoverImage(req.getCoverImage() == null ? "" : req.getCoverImage());
        hackathon.setPrizePool(req.getPrizePool());
        hackathon.setStartTime(req.getStartTime());
        hackathon.setEndTime(req.getEndTime());
        hackathon.setSignupDeadline(req.getSignupDeadline());
        hackathon.setLocation(req.getLocation() == null ? "" : req.getLocation());
        hackathon.setMaxTeams(req.getMaxTeams() == null ? 0 : req.getMaxTeams());
    }

    private Map<Long, String> loadNicknames(List<Hackathon> records) {
        Map<Long, String> map = new HashMap<>();
        if (records == null || records.isEmpty()) {
            return map;
        }
        List<Long> userIds = records.stream()
                .map(Hackathon::getPublishedBy)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        for (Long uid : userIds) {
            User u = userMapper.selectById(uid);
            if (u != null) {
                map.put(uid, u.getNickname());
            }
        }
        return map;
    }

    private AdminHackathonVO toVO(Hackathon h, String publisherNickname) {
        AdminHackathonVO vo = new AdminHackathonVO();
        vo.setId(h.getId());
        vo.setTitle(h.getTitle());
        vo.setDescription(h.getDescription());
        vo.setCoverImage(h.getCoverImage());
        vo.setPrizePool(h.getPrizePool());
        vo.setStartTime(h.getStartTime());
        vo.setEndTime(h.getEndTime());
        vo.setSignupDeadline(h.getSignupDeadline());
        vo.setLocation(h.getLocation());
        vo.setMaxTeams(h.getMaxTeams());
        vo.setCurrentTeams(h.getCurrentTeams());
        vo.setStatus(h.getStatus());
        vo.setPublishedBy(h.getPublishedBy());
        vo.setPublisherNickname(publisherNickname);
        vo.setCreatedAt(h.getCreatedAt());
        vo.setUpdatedAt(h.getUpdatedAt());
        return vo;
    }
}
