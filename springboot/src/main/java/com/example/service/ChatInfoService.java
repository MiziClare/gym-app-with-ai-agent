package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Account;
import com.example.entity.ChatInfo;
import com.example.entity.Coach;
import com.example.entity.User;
import com.example.mapper.ChatInfoMapper;
import com.example.mapper.CoachMapper;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Chat information service
 **/
@Service
public class ChatInfoService {

    @Resource
    private ChatInfoMapper chatInfoMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private CoachMapper coachMapper;

    /**
     * Insert
     */
    public void add(ChatInfo chatInfo) {
        chatInfo.setTime(DateUtil.now());
        
        // Set the default read status to "Unread"
        if (chatInfo.getIsread() == null) {
            chatInfo.setIsread("Unread");
        }
        
        // Ensure correct role information
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser != null && chatInfo.getRole() == null) {
            chatInfo.setRole(currentUser.getRole());
        }
        
        chatInfoMapper.insert(chatInfo);
    }

    /**
     * Delete
     */
    public void deleteById(Integer id) {
        chatInfoMapper.deleteById(id);
    }

    /**
     * Batch delete
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            chatInfoMapper.deleteById(id);
        }
    }

    /**
     * Update
     */
    public void updateById(ChatInfo chatInfo) {
        chatInfoMapper.updateById(chatInfo);
    }

    /**
     * Query by ID
     */
    public ChatInfo selectById(Integer id) {
        return chatInfoMapper.selectById(id);
    }

    /**
     * Query all
     */
    public List<ChatInfo> selectAll(ChatInfo chatInfo) {
        return chatInfoMapper.selectAll(chatInfo);
    }

    /**
     * Page query
     */
    public PageInfo<ChatInfo> selectPage(ChatInfo chatInfo, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ChatInfo> list = chatInfoMapper.selectAll(chatInfo);
        return PageInfo.of(list);
    }

    public Integer selectUnReadChatNum(Integer userId, Integer chatUserId,String role) {
        return chatInfoMapper.selectUnReadChatNum(userId, chatUserId,role);
    }

    public List<ChatInfo> selectUserChat(Integer chatUserId) {
        Account currentUser = TokenUtils.getCurrentUser();
        Integer userId = currentUser.getId();
        List<ChatInfo> chatInfos = chatInfoMapper.selectUserChatById(userId, chatUserId);
        
        for (ChatInfo chatInfo : chatInfos) {
            // The current logged-in user is a normal user, chatting with the coach
            if (currentUser.getRole().equals("USER")) {
                if (chatInfo.getUserId().equals(userId)) {
                    // The user's message
                    User user = userMapper.selectById(chatInfo.getUserId());
                    if (ObjectUtil.isNotEmpty(user)) {
                        chatInfo.setUserName(user.getName());
                        chatInfo.setUserAvatar(user.getAvatar());
                    }
                    
                    // The coach who received the message
                    Coach coach = coachMapper.selectById(chatInfo.getChatUserId());
                    if (ObjectUtil.isNotEmpty(coach)) {
                        chatInfo.setChatUserName(coach.getName());
                        chatInfo.setChatUserAvatar(coach.getAvatar());
                    }
                } else {
                    // The coach's message
                    Coach coach = coachMapper.selectById(chatInfo.getUserId());
                    if (ObjectUtil.isNotEmpty(coach)) {
                        chatInfo.setUserName(coach.getName());
                        chatInfo.setUserAvatar(coach.getAvatar());
                    }
                    
                    // The user who received the message
                    User user = userMapper.selectById(chatInfo.getChatUserId());
                    if (ObjectUtil.isNotEmpty(user)) {
                        chatInfo.setChatUserName(user.getName());
                        chatInfo.setChatUserAvatar(user.getAvatar());
                    }
                }
            } 
            // The current logged-in user is a coach, chatting with a normal user
            else if (currentUser.getRole().equals("COACH")) {
                if (chatInfo.getUserId().equals(userId)) {
                    // The coach's message
                    Coach coach = coachMapper.selectById(chatInfo.getUserId());
                    if (ObjectUtil.isNotEmpty(coach)) {
                        chatInfo.setUserName(coach.getName());
                        chatInfo.setUserAvatar(coach.getAvatar());
                    }
                    
                    // The user who received the message
                    User user = userMapper.selectById(chatInfo.getChatUserId());
                    if (ObjectUtil.isNotEmpty(user)) {
                        chatInfo.setChatUserName(user.getName());
                        chatInfo.setChatUserAvatar(user.getAvatar());
                    }
                } else {
                    // The user's message
                    User user = userMapper.selectById(chatInfo.getUserId());
                    if (ObjectUtil.isNotEmpty(user)) {
                        chatInfo.setUserName(user.getName());
                        chatInfo.setUserAvatar(user.getAvatar());
                    }
                    
                    // The coach who received the message
                    Coach coach = coachMapper.selectById(chatInfo.getChatUserId());
                    if (ObjectUtil.isNotEmpty(coach)) {
                        chatInfo.setChatUserName(coach.getName());
                        chatInfo.setChatUserAvatar(coach.getAvatar());
                    }
                }
            }
        }
        return chatInfos;
    }

    public void updateRead(Integer chatUserId) {
        Account currentUser = TokenUtils.getCurrentUser();
        chatInfoMapper.updateRead(currentUser.getId(), chatUserId);
    }

}