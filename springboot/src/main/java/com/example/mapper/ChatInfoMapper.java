package com.example.mapper;

import com.example.entity.ChatInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Interface for operating chat_info related data
 */
public interface ChatInfoMapper {

    /**
     * Insert
     */
    int insert(ChatInfo chatInfo);

    /**
     * Delete
     */
    int deleteById(Integer id);

    /**
     * Update
     */
    int updateById(ChatInfo chatInfo);

    /**
     * Query by ID
     */
    ChatInfo selectById(Integer id);

    /**
     * Query all
     */
    List<ChatInfo> selectAll(ChatInfo chatInfo);

    @Select("select count(*) from chat_info where user_id = #{chatUserId} and chat_user_id = #{userId} and isread = 'Unread' and role = #{role}")
    Integer selectUnReadChatNum(@Param("userId") Integer userId, @Param("chatUserId") Integer chatUserId, @Param("role") String role);

    List<ChatInfo> selectUserChat(@Param("userId") Integer userId, @Param("chatUserId") Integer chatUserId);

    List<ChatInfo> selectUserChatBusiness(@Param("userId") Integer userId, @Param("chatUserId") Integer chatUserId);

    List<ChatInfo> selectUserChatById(@Param("userId") Integer userId, @Param("chatUserId") Integer chatUserId);

    @Update("update chat_info set isread = 'Read' where user_id = #{chatUserId} and chat_user_id = #{userId}")
    void updateRead(@Param("userId") Integer userId, @Param("chatUserId") Integer chatUserId);

}