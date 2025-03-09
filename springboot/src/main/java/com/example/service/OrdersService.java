package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.CoachMapper;
import com.example.mapper.CourseMapper;
import com.example.mapper.OrdersMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Order table business processing
 **/
@Service
public class OrdersService {

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private CoachMapper coachMapper;

    /**
     * Add
     */
    public void add(Orders orders) {
        // First, check if the user's balance is sufficient
        User user = userMapper.selectById(orders.getUserId());
        if (user.getAccount() < orders.getPrice()) {
            throw new CustomException("-1", "Insufficient balance. Please top up in your account.");
        }
        orders.setTime(DateUtil.now());
        orders.setOrderNo(DateUtil.format(new Date(), "yyyyMMddHHmmss"));
        ordersMapper.insert(orders);

        // Deduct the user's balance
        user.setAccount(user.getAccount() - orders.getPrice());
        userMapper.updateById(user);

    }

    /**
     * Delete
     */
    public void deleteById(Integer id) {
        ordersMapper.deleteById(id);
    }

    /**
     * Batch delete
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            ordersMapper.deleteById(id);
        }
    }

    /**
     * Update
     */
    public void updateById(Orders orders) {
        ordersMapper.updateById(orders);
    }

    /**
     * Query by ID
     */
    public Orders selectById(Integer id) {
        return ordersMapper.selectById(id);
    }

    /**
     * Query all
     */
    public List<Orders> selectAll(Orders orders) {
        return ordersMapper.selectAll(orders);
    }

    /**
     * Pagination query
     */
    public PageInfo<Orders> selectPage(Orders orders, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> list = ordersMapper.selectAll(orders);
        for (Orders dbOrders : list) {
            Integer userId = dbOrders.getUserId();
            User user = userMapper.selectById(userId);
            if (ObjectUtil.isNotEmpty(user)) {
                dbOrders.setUserName(user.getName());
            }
            Course course = courseMapper.selectById(dbOrders.getCourseId());
            if (ObjectUtil.isNotEmpty(course)) {
                dbOrders.setCourseName(course.getName());
                Coach coach = coachMapper.selectById(course.getCoachId());
                if (ObjectUtil.isNotEmpty(coach)) {
                    dbOrders.setCoachName(coach.getName());
                    dbOrders.setCoachId(coach.getId());
                }
            }
        }
        return PageInfo.of(list);
    }

}