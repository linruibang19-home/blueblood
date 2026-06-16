package com.blueblood.api.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blueblood.api.modules.user.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
