package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Account;
import com.example.entity.ChatGroup;
import com.example.entity.Coach;
import com.example.entity.User;
import com.example.mapper.ChatGroupMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Chat group service
 **/
@Service
public class ChatGroupService {

    @Resource
    private ChatGroupMapper chatGroupMapper;
    @Resource
    private ChatInfoService chatInfoService;
    @Resource
    private UserService userService;

    @Resource
    private CoachService coachService;

    /**
     * Insert
     */
    public void add(ChatGroup chatGroup) {
        ChatGroup dbChatGroup = chatGroupMapper.selectByChatUserIdAndUserId(chatGroup.getChatUserId(), chatGroup.getUserId(),"USER");
        if (dbChatGroup == null) {
            chatGroupMapper.insert(chatGroup);
        }
        ChatGroup dbChatGroup1 = chatGroupMapper.selectByChatUserIdAndUserId(chatGroup.getUserId(), chatGroup.getChatUserId(),"COACH");
        if (dbChatGroup1 == null) {
            dbChatGroup1 = new ChatGroup();
            dbChatGroup1.setChatUserId(chatGroup.getUserId());
            dbChatGroup1.setUserId(chatGroup.getChatUserId());
            dbChatGroup1.setRole("COACH");
            chatGroupMapper.insert(dbChatGroup1);
        }
    }

    /**
     * Delete
     */
    public void deleteById(Integer id) {
        chatGroupMapper.deleteById(id);
    }

    /**
     * Batch delete
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            chatGroupMapper.deleteById(id);
        }
    }

    /**
     * Update
     */
    public void updateById(ChatGroup chatGroup) {
        chatGroupMapper.updateById(chatGroup);
    }

    /**
     * Query by ID
     */
    public ChatGroup selectById(Integer id) {
        return chatGroupMapper.selectById(id);
    }

    /**
     * Query all
     */
    public List<ChatGroup> selectAll(ChatGroup chatGroup) {
        return chatGroupMapper.selectAll(chatGroup);
    }

    /**
     * Page query
     */
    public PageInfo<ChatGroup> selectPage(ChatGroup chatGroup, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ChatGroup> list = chatGroupMapper.selectAll(chatGroup);
        return PageInfo.of(list);
    }

    public List<ChatGroup> selectUserGroup() {
        List<ChatGroup> chatGroupList = new ArrayList<>();
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser.getRole().equals("USER")) {
            List<ChatGroup> userChatGroupList = chatGroupMapper.selectByUserId(currentUser.getId(), currentUser.getRole());
            for (ChatGroup chatGroup : userChatGroupList) {
                Integer chatUserId = chatGroup.getChatUserId();
                Integer num = chatInfoService.selectUnReadChatNum(currentUser.getId(), chatUserId,"COACH");
                chatGroup.setChatNum(num);
                Coach chatCoach = coachService.selectById(chatUserId);
                if (ObjectUtil.isNotEmpty(chatCoach)) {
                    chatGroup.setChatUserName(chatCoach.getName());
                    chatGroup.setChatUserAvatar(chatCoach.getAvatar());
                    chatGroupList.add(chatGroup);
                }
            }
        } else {
            List<ChatGroup> userChatGroupList = chatGroupMapper.selectByUserId(currentUser.getId(), currentUser.getRole());
            for (ChatGroup chatGroup : userChatGroupList) {
                Integer chatUserId = chatGroup.getChatUserId();
                Integer num = chatInfoService.selectUnReadChatNum(currentUser.getId(), chatUserId,"USER");
                chatGroup.setChatNum(num);
                User chatUser = userService.selectById(chatUserId);
                if (ObjectUtil.isNotEmpty(chatUser)) {
                    chatGroup.setChatUserName(chatUser.getName());
                    chatGroup.setChatUserAvatar(chatUser.getAvatar());
                    chatGroupList.add(chatGroup);
                }

            }
        }

        return chatGroupList;
    }

    /**
     * Determine if the chat group already exists
     * @param chatGroup
     * @return
     */
    public Boolean isChatGroupById(ChatGroup chatGroup) {
        ChatGroup chatGroupById = chatGroupMapper.isChatGroupById(chatGroup);
        if (ObjectUtil.isNotEmpty(chatGroupById)) {
            return true;
        } else {
            return false;
        }
    }
}