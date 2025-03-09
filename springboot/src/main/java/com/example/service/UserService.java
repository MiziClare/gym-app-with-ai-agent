package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.common.Constants;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.Account;
import com.example.entity.Orders;
import com.example.entity.User;
import com.example.entity.Course;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.example.mapper.CourseMapper;
import com.example.common.enums.RoleEnum;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    // UserMapper is a class that maps the user table in the database
    // Inject UserMapper through @Resource annotation, and all database operations are completed by userMapper
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private OrdersService ordersService;

    @Resource
    private CourseMapper courseMapper;

    /**
     * Add
     */
    public void add(User user) {
        User dbUser = userMapper.selectByUsername(user.getUsername());
        // check if the username already exists
        if (ObjectUtil.isNotNull(dbUser)) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        // if the password is empty, set the default password
        if (ObjectUtil.isEmpty(user.getPassword())) {
            user.setPassword(Constants.USER_DEFAULT_PASSWORD);
        }
        // if the name is empty, let the name be the same as the username
        if (ObjectUtil.isEmpty(user.getName())) {
            user.setName(user.getUsername());
        }
        // set the role 'USER' to user
        user.setRole(RoleEnum.USER.name());
        // insert only when the user does not exist
        userMapper.insert(user);
    }

    /**
     * Delete
     */
    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    /**
     * Batch delete
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            userMapper.deleteById(id);
        }
    }

    /**
     * Update
     */
    public void updateById(User user) {
        userMapper.updateById(user);
    }

    /**
     * Query by ID
     */
    public User selectById(Integer id) {
        User dbUser = userMapper.selectById(id);
        // Regenerate the token every time because the token is invalid after the user information is modified
        String tokenData = dbUser.getId() + "-" + RoleEnum.USER.name();
        String token = TokenUtils.createToken(tokenData, dbUser.getPassword());
        dbUser.setToken(token);
        return dbUser;
    }

    /**
     * Query all
     */
    public List<User> selectAll(User user) {
        return userMapper.selectAll(user);
    }

    /**
     * Pagination query
     */
    public PageInfo<User> selectPage(User user, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectAll(user);
        return PageInfo.of(list);
    }

    /**
     * Login
     */
    public Account login(Account account) {
        Account dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        // 生成token
        String tokenData = dbUser.getId() + "-" + RoleEnum.USER.name();
        String token = TokenUtils.createToken(tokenData, dbUser.getPassword());
        dbUser.setToken(token);
        return dbUser;
    }

    /**
     * Register
     */
    public void register(Account account) {
        User user = new User();
        BeanUtils.copyProperties(account, user);
        add(user);
    }

    /**
     * Update password
     */
    public void updatePassword(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        dbUser.setPassword(account.getNewPassword());
        userMapper.updateById(dbUser);
    }

    /**
     * Reset password
     */
    public void recharge(Double account) {
        Account currentUser = TokenUtils.getCurrentUser();
        // 1. Query the detailed information of the user from the database according to the user id
        User user = userMapper.selectById(currentUser.getId());
        // 2. Update the user's balance
        user.setAccount(user.getAccount() + account);
        userMapper.updateById(user);
    }

    /**
     * Export user information as CSV format
     * @param userId User ID
     * @return Contains user information in CSV format
     */
    public String exportUserInfoToCsv(Integer userId) {
        // Query user detailed information
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        
        // Query the user's order information
        Orders orderQuery = new Orders();
        orderQuery.setUserId(userId);
        List<Orders> ordersList = ordersService.selectAll(orderQuery);
        
        // Fill the course name for each order
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders order : ordersList) {
                if (order.getCourseId() != null) {
                    Course course = courseMapper.selectById(order.getCourseId());
                    if (course != null) {
                        order.setCourseName(course.getName());
                    }
                }
            }
        }
        
        // Build the order information string
        String ordersInfo = "";
        if (ordersList != null && !ordersList.isEmpty()) {
            ordersInfo = ordersList.stream()
                .map(order -> String.format("Order #%s: %s - £%.2f (%s)", 
                    order.getOrderNo(), 
                    order.getCourseName() != null ? order.getCourseName() : "Unknown Course",
                    order.getPrice() != null ? order.getPrice() : 0.0,
                    order.getTime() != null ? order.getTime() : "Unknown Time"))
                .collect(Collectors.joining("; "));
        }
        
        // Build the CSV header
        StringBuilder csv = new StringBuilder();
        csv.append("ID,username,name,role,phone,email,balance,orders\n");
        
        // Add the user data line, ensuring all fields are null checked
        csv.append(user.getId() != null ? user.getId() : "").append(",")
           .append(user.getUsername() != null ? user.getUsername() : "").append(",")
           .append(user.getName() != null ? user.getName() : "").append(",")
           .append(user.getRole() != null ? user.getRole() : "").append(",")
           .append(user.getPhone() != null ? user.getPhone() : "").append(",")
           .append(user.getEmail() != null ? user.getEmail() : "").append(",")
           .append(user.getAccount() != null ? user.getAccount() : 0.0).append(",")
           .append("\"").append(ordersInfo).append("\"").append("\n");
        
        return csv.toString();
    }

    // Keep the original method as an overload, and call the new method internally
    public String exportUserInfoToCsv() {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        return exportUserInfoToCsv(currentUser.getId());
    }

    /**
     * Delete user personal data (soft delete)
     * Set the name, phone, and email of the user to an empty string
     */
    public void deletePersonalData(User user) {
        // Get the user information from the database
        User dbUser = userMapper.selectById(user.getId());
        if (dbUser == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        
        // Only update the fields that need to be cleared
        dbUser.setName("");
        dbUser.setPhone("");
        dbUser.setEmail("");
        
        // Update to the database
        userMapper.updateById(dbUser);
    }
}
