package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.common.enums.RoleEnum;
import com.example.entity.Coach;
import com.example.entity.Experience;
import com.example.entity.User;
import com.example.mapper.CoachMapper;
import com.example.mapper.ExperienceMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * Fitness experience table business processing
 **/
@Service
public class ExperienceService {

    @Resource
    private ExperienceMapper experienceMapper;
    @Resource
    private CoachMapper coachMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * Add
     */
    public void add(Experience experience) {
        experience.setTime(DateUtil.now());
        experienceMapper.insert(experience);
    }

    /**
     * Delete
     */
    public void deleteById(Integer id) {
        experienceMapper.deleteById(id);
    }

    /**
     * Batch delete
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            experienceMapper.deleteById(id);
        }
    }

    /**
     * Update
     */
    public void updateById(Experience experience) {
        experienceMapper.updateById(experience);
    }

    /**
     * Query by ID
     */
    public Experience selectById(Integer id) {
        Experience experience = experienceMapper.selectById(id);
        extracted(experience);
        return experience;
    }

    private void extracted(Experience experience) {
        if (RoleEnum.COACH.name().equals(experience.getRole())) {
            // Query the corresponding coach in the coach table
            Coach coach = coachMapper.selectById(experience.getUserId());
            experience.setUserName(coach.getName());
        } else {
            // Query the corresponding user in the user table
            User user = userMapper.selectById(experience.getUserId());
            experience.setUserName(user.getName());
        }
    }

    /**
     * Query all
     */
    public List<Experience> selectAll(Experience experience) {
        return experienceMapper.selectAll(experience);
    }

    /**
     * Pagination query
     */
    public PageInfo<Experience> selectPage(Experience experience, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Experience> list = experienceMapper.selectAll(experience);
        for (Experience dbExperience : list) {
            extracted(dbExperience);
        }
        return PageInfo.of(list);
    }

}