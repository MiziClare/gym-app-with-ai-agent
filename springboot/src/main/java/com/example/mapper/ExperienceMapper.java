package com.example.mapper;

import com.example.entity.Experience;

import java.util.List;

/**
 * 操作experience相关数据接口
 */
public interface ExperienceMapper {

    /**
     * 新增
     */
    int insert(Experience experience);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Experience experience);

    /**
     * 根据ID查询
     */
    Experience selectById(Integer id);

    /**
     * 查询所有
     */
    List<Experience> selectAll(Experience experience);

}