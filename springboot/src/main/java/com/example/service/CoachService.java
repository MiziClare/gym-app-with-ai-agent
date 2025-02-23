package com.example.service;

import com.example.entity.Coach;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.exception.CustomException;
import com.example.mapper.CoachMapper;
import com.example.common.Constants;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import cn.hutool.core.util.ObjectUtil;
import java.util.List;

import javax.annotation.Resource;

@Service
public class CoachService {

    @Resource
    private CoachMapper coachMapper;

    /**
     * 新增
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
     * 删除
     */
    public void deleteById(Integer id) {
        coachMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            coachMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Coach coach) {
        coachMapper.updateById(coach);
    }

    /**
     * 根据ID查询
     */
    public Coach selectById(Integer id) {
        return coachMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Coach> selectAll(Coach coach) {
        return coachMapper.selectAll(coach);
    }

    /**
     * 分页查询
     */
    public PageInfo<Coach> selectPage(Coach coach, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Coach> list = coachMapper.selectAll(coach);
        return PageInfo.of(list);
    }

}
