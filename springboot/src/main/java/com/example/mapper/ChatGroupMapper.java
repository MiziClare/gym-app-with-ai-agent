package com.example.mapper;

import com.example.entity.ChatGroup;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Interface for operating chat_group related data
 */
public interface ChatGroupMapper {

    /**
     * Insert
     */
    int insert(ChatGroup chatGroup);

    /**
     * Delete
     */
    int deleteById(Integer id);

    /**
     * Update
     */
    int updateById(ChatGroup chatGroup);

    /**
     * Query by ID
     */
    ChatGroup selectById(Integer id);

    /**
     * Query all
     */
    List<ChatGroup> selectAll(ChatGroup chatGroup);

    @Select("select * from chat_group where user_id = #{userId} and role = #{role}")
    List<ChatGroup> selectByUserId(Integer userId, String role);

    @Select("select * from chat_group where chat_user_id = #{chatUserId} and user_id = #{userId} and role = #{role}")
    ChatGroup selectByChatUserIdAndUserId(@Param("chatUserId") Integer chatUserId, @Param("userId") Integer userId, @Param("role") String role);

    ChatGroup isChatGroupById(ChatGroup chatGroup);
}