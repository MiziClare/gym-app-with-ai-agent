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
 * API:
 * Add user (POST /user/add)
 * Delete user (DELETE /user/delete/{id})
 * Batch delete user (DELETE /user/delete/batch)
 * Update user (PUT /user/update)
 * Query single user (GET /user/selectById/{id})
 * Query all users (GET /user/selectAll)
 * Query by page (GET /user/selectPage)
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
     * Add
     */
    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        userService.add(user);
        return Result.success();
    }

    /**
     * Delete
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        userService.deleteById(id);
        return Result.success();
    }

    /**
     * Batch delete
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        userService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * Update
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody User user) {
        userService.updateById(user);
        return Result.success();
    }

    /**
     * Query by ID
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        User user = userService.selectById(id);
        return Result.success(user);
    }

    /**
     * Query all
     */
    @GetMapping("/selectAll")
    public Result selectAll(User user ) {
        List<User> list = userService.selectAll(user);
        return Result.success(list);
    }

    /**
     * Query by page
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
     * Let the user export their personal information
     * Download data in CSV file format
     */
    @GetMapping("/exportUserInfo")
    public ResponseEntity<byte[]> exportUserInfo(@RequestParam(required = false) Integer userId) {
        try {
            // If userId is not passed in, try to get it from the token
            if (userId == null) {
                Account currentUser = TokenUtils.getCurrentUser();
                if (currentUser != null && currentUser.getId() != null) {
                    userId = currentUser.getId();
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
            
            // Get the CSV string of user information
            String csvData = userService.exportUserInfoToCsv(userId);
            
            // Set HTTP response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=user_info.csv");
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("Content-Type", "text/csv");
            
            // Convert the CSV string to a byte array and return it
            byte[] csvBytes = csvData.getBytes(StandardCharsets.UTF_8);
            
            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete user personal data (soft delete)
     */
    @PutMapping("/deletePersonalData")
    public Result deletePersonalData(@RequestBody User user) {
        userService.deletePersonalData(user);
        return Result.success();
    }
}
