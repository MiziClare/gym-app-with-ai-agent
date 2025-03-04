package com.example.controller;

import com.example.common.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import com.example.entity.User;
import com.example.service.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.github.pagehelper.PageInfo;
import java.util.HashMap;
import java.util.Map;
import com.example.entity.Orders;
import com.example.service.OrdersService;
import com.example.entity.Reserve;
import com.example.service.ReserveService;
import com.example.entity.Eqreserve;
import com.example.service.EqreserveService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import com.example.entity.Account;
import com.example.utils.TokenUtils;

/**
 * API：
 * 新增用户 (POST /user/add)
 * 删除用户 (DELETE /user/delete/{id})
 * 批量删除用户 (DELETE /user/delete/batch)
 * 修改用户 (PUT /user/update)
 * 查询单个用户 (GET /user/selectById/{id})
 * 查询所有用户 (GET /user/selectAll)
 * 分页查询用户 (GET /user/selectPage)
 */

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private OrdersService ordersService;

    @Resource
    private ReserveService reserveService;

    @Resource
    private EqreserveService eqreserveService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        userService.add(user);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        userService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        userService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        User user = userService.selectById(id);
        return Result.success(user);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(User user ) {
        List<User> list = userService.selectAll(user);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(User user,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<User> page = userService.selectPage(user, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/recharge/{account}")
    public Result recharge(@PathVariable Double account) {
        userService.recharge(account);
        return Result.success();
    }

    /**
     * 让用户导出个人信息
     * 以CSV文件形式下载数据
     */
    @GetMapping("/exportUserInfo")
    public ResponseEntity<byte[]> exportUserInfo(@RequestParam(required = false) Integer userId) {
        try {
            // 如果没有传入userId，尝试从token中获取
            if (userId == null) {
                Account currentUser = TokenUtils.getCurrentUser();
                if (currentUser != null && currentUser.getId() != null) {
                    userId = currentUser.getId();
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
            
            // 获取用户信息的CSV字符串
            String csvData = userService.exportUserInfoToCsv(userId);
            
            // 设置HTTP响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=user_info.csv");
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("Content-Type", "text/csv");
            
            // 将CSV字符串转换为字节数组并返回
            byte[] csvBytes = csvData.getBytes(StandardCharsets.UTF_8);
            
            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
