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
 * 健身经验表业务处理
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
     * 新增
     */
    public void add(Experience experience) {
        experience.setTime(DateUtil.now());
        experienceMapper.insert(experience);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        experienceMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            experienceMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Experience experience) {
        experienceMapper.updateById(experience);
    }

    /**
     * 根据ID查询
     */
    public Experience selectById(Integer id) {
        Experience experience = experienceMapper.selectById(id);
        extracted(experience);
        return experience;
    }

    private void extracted(Experience experience) {
        if (RoleEnum.COACH.name().equals(experience.getRole())) {
            // 去教练表里查询对应的教练
            Coach coach = coachMapper.selectById(experience.getUserId());
            experience.setUserName(coach.getName());
        } else {
            // 去用户表里查询对应的用户
            User user = userMapper.selectById(experience.getUserId());
            experience.setUserName(user.getName());
        }
    }

    /**
     * 查询所有
     */
    public List<Experience> selectAll(Experience experience) {
        return experienceMapper.selectAll(experience);
    }

    /**
     * 分页查询
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