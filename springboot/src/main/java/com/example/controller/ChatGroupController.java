package com.example.controller;

import com.example.common.Result;
import com.example.entity.ChatGroup;
import com.example.service.ChatGroupService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Chat group controller
 **/
@RestController
@RequestMapping("/chatGroup")
public class ChatGroupController {

    @Resource
    private ChatGroupService chatGroupService;

    /**
     * Insert
     */
    @PostMapping("/add")
    public Result add(@RequestBody ChatGroup chatGroup) {
        chatGroupService.add(chatGroup);
        return Result.success();
    }

    /**
     * Delete
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        chatGroupService.deleteById(id);
        return Result.success();
    }

    /**
     * Batch delete
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        chatGroupService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * Update
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody ChatGroup chatGroup) {
        chatGroupService.updateById(chatGroup);
        return Result.success();
    }

    /**
     * Query by ID
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        ChatGroup chatGroup = chatGroupService.selectById(id);
        return Result.success(chatGroup);
    }

    /**
     * Query all
     */
    @GetMapping("/selectAll")
    public Result selectAll(ChatGroup chatGroup) {
        List<ChatGroup> list = chatGroupService.selectAll(chatGroup);
        return Result.success(list);
    }

    /**
     * Page query
     */
    @GetMapping("/selectPage")
    public Result selectPage(ChatGroup chatGroup,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<ChatGroup> page = chatGroupService.selectPage(chatGroup, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * Chat list
     */
    @GetMapping("/selectUserGroup")
    public Result selectUserGroup() {
        List<ChatGroup> chatGroupList = chatGroupService.selectUserGroup();
        return Result.success(chatGroupList);
    }

    /**
     * Determine if the chat group already exists
     */
    @PostMapping("/isChatGroupById")
    public Result isChatGroupById(@RequestBody ChatGroup chatGroup) {
        Boolean chatGroupById = chatGroupService.isChatGroupById(chatGroup);
        if (chatGroupById) {
            return Result.success();
        } else {
            return Result.error();
        }
    }

}