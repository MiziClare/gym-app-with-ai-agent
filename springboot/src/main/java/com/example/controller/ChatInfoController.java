package com.example.controller;

import com.example.common.Result;
import com.example.entity.ChatInfo;
import com.example.service.ChatInfoService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import com.example.entity.Account;
import com.example.utils.TokenUtils;

/**
 * Chat information controller
 **/
@RestController
@RequestMapping("/chatInfo")
public class ChatInfoController {

    @Resource
    private ChatInfoService chatInfoService;

    /**
     * Insert
     */
    @PostMapping("/add")
    public Result add(@RequestBody ChatInfo chatInfo) {
        Account currentUser = TokenUtils.getCurrentUser();
        
        // Force set the role to the current user's role
        chatInfo.setRole(currentUser.getRole());
        
        chatInfoService.add(chatInfo);
        return Result.success();
    }

    /**
     * Delete
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        chatInfoService.deleteById(id);
        return Result.success();
    }

    /**
     * Batch delete
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        chatInfoService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * Update
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody ChatInfo chatInfo) {
        chatInfoService.updateById(chatInfo);
        return Result.success();
    }

    @PutMapping("/updateRead/{chatUserId}")
    public Result updateRead(@PathVariable Integer chatUserId) {
        chatInfoService.updateRead(chatUserId);
        return Result.success();
    }

    /**
     * Query by ID
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        ChatInfo chatInfo = chatInfoService.selectById(id);
        return Result.success(chatInfo);
    }

    /**
     * Query all
     */
    @GetMapping("/selectAll")
    public Result selectAll(ChatInfo chatInfo) {
        List<ChatInfo> list = chatInfoService.selectAll(chatInfo);
        return Result.success(list);
    }

    /**
     * Page query
     */
    @GetMapping("/selectPage")
    public Result selectPage(ChatInfo chatInfo,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<ChatInfo> page = chatInfoService.selectPage(chatInfo, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * Query chat history
     */
    @GetMapping("/selectUserChat/{chatUserId}")
    public Result selectUserChat(@PathVariable Integer chatUserId) {
        List<ChatInfo> chatInfoList = chatInfoService.selectUserChat(chatUserId);
        return Result.success(chatInfoList);
    }

}