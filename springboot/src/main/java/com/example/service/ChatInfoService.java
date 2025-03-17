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
            if (chatInfo.getRole().equals("USER")) {
                User user = userMapper.selectById(chatInfo.getUserId());
                if (ObjectUtil.isNotEmpty(user)) {
                    chatInfo.setUserName(user.getName());
                    chatInfo.setUserAvatar(user.getAvatar());
                }
                Coach coach = coachMapper.selectById(chatInfo.getChatUserId());
                if (ObjectUtil.isNotEmpty(coach)) {
                    chatInfo.setChatUserName(coach.getName());
                    chatInfo.setChatUserAvatar(coach.getAvatar());
                }
            } else {
                Coach coach = coachMapper.selectById(chatInfo.getUserId());
                if (ObjectUtil.isNotEmpty(coach)) {
                    chatInfo.setUserName(coach.getName());
                    chatInfo.setUserAvatar(coach.getAvatar());
                }
                User user = userMapper.selectById(chatInfo.getChatUserId());
                if (ObjectUtil.isNotEmpty(user)) {
                    chatInfo.setChatUserName(user.getName());
                    chatInfo.setChatUserAvatar(user.getAvatar());
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