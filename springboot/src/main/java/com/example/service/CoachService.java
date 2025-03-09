package com.example.service;

import com.example.entity.Account;
import com.example.entity.Coach;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.exception.CustomException;
import com.example.mapper.CoachMapper;
import com.example.common.Constants;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.ObjectUtil;
import java.util.List;

import javax.annotation.Resource;

@Service
public class CoachService {

    @Resource
    private CoachMapper coachMapper;

    /**
     * Add
     */
    public void add(Coach coach) {
        Coach dbCoach = coachMapper.selectByUsername(coach.getUsername());
        if (ObjectUtil.isNotNull(dbCoach)) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        if (ObjectUtil.isEmpty(coach.getPassword())) {
            coach.setPassword(Constants.USER_DEFAULT_PASSWORD);
        }
        if (ObjectUtil.isEmpty(coach.getName())) {
            coach.setName(coach.getUsername());
        }
        coach.setRole(RoleEnum.COACH.name());
        coachMapper.insert(coach);
    }

    /**
     * Delete
     */
    public void deleteById(Integer id) {
        coachMapper.deleteById(id);
    }

    /**
     * Batch delete
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            coachMapper.deleteById(id);
        }
    }

    /**
     * Update
     */
    public void updateById(Coach coach) {
        coachMapper.updateById(coach);
    }

    /**
     * Query by ID
     */
    public Coach selectById(Integer id) {
        return coachMapper.selectById(id);
    }

    /**
     * Query all
     */
    public List<Coach> selectAll(Coach coach) {
        return coachMapper.selectAll(coach);
    }

    /**
     * Pagination query
     */
    public PageInfo<Coach> selectPage(Coach coach, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Coach> list = coachMapper.selectAll(coach);
        return PageInfo.of(list);
    }

    /**
     * Login
     */
    public Account login(Account account) {
        Account dbCoach = coachMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbCoach)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbCoach.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        // 生成token
        String tokenData = dbCoach.getId() + "-" + RoleEnum.COACH.name();
        String token = TokenUtils.createToken(tokenData, dbCoach.getPassword());
        dbCoach.setToken(token);
        return dbCoach;
    }

    /**
     * Register
     */
    public void register(Account account) {
        Coach coach = new Coach();
        BeanUtils.copyProperties(account, coach);
        add(coach);
    }

    /**
     * Reset password
     */
    public void updatePassword(Account account) {
        Coach dbCoach = coachMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbCoach)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbCoach.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        dbCoach.setPassword(account.getNewPassword());
        coachMapper.updateById(dbCoach);
    }
}
