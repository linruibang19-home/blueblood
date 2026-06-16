package com.blueblood.api.modules.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blueblood.api.modules.community.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
