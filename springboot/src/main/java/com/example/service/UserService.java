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
    // 通过 @Resource 注解，将 UserMapper 注入进来，后续所有对数据库的操作都由 userMapper 完成。
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private OrdersService ordersService;

    @Resource
    private CourseMapper courseMapper;

    /**
     * 新增
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
        // set the role ‘USER’ to user
        user.setRole(RoleEnum.USER.name());
        // insert only when the user does not exist
        userMapper.insert(user);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            userMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(User user) {
        userMapper.updateById(user);
    }

    /**
     * 根据ID查询
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
     * 查询所有
     */
    public List<User> selectAll(User user) {
        return userMapper.selectAll(user);
    }

    /**
     * 分页查询
     */
    public PageInfo<User> selectPage(User user, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectAll(user);
        return PageInfo.of(list);
    }

    /**
     * 登录
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
     * 注册
     */
    public void register(Account account) {
        User user = new User();
        BeanUtils.copyProperties(account, user);
        add(user);
    }

    /**
     * 修改密码
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
     * 重置密码
     */
    public void recharge(Double account) {
        Account currentUser = TokenUtils.getCurrentUser();
        // 1.根据用户id从数据库中查询出用户的详细信息
        User user = userMapper.selectById(currentUser.getId());
        // 2. 更新用户的余额
        user.setAccount(user.getAccount() + account);
        userMapper.updateById(user);
    }

    /**
     * 导出用户信息为CSV格式
     * @param userId 用户ID
     * @return 包含用户信息的CSV字符串
     */
    public String exportUserInfoToCsv(Integer userId) {
        // 查询用户详细信息
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        
        // 查询用户的订单信息
        Orders orderQuery = new Orders();
        orderQuery.setUserId(userId);
        List<Orders> ordersList = ordersService.selectAll(orderQuery);
        
        // 为每个订单填充课程名称
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
        
        // 构建订单信息字符串
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
        
        // 构建CSV头部
        StringBuilder csv = new StringBuilder();
        csv.append("ID,username,name,role,phone,email,balance,orders\n");
        
        // 添加用户数据行，确保所有字段都进行null检查
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

    // 保留原方法作为重载，内部调用新方法
    public String exportUserInfoToCsv() {
        Account currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        return exportUserInfoToCsv(currentUser.getId());
    }
}
