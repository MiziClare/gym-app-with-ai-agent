package com.example.mapper;

import com.example.entity.Equipment;

import java.util.List;

/**
 * 操作equipment相关数据接口
 */
public interface EquipmentMapper {

    /**
     * 新增
     */
    int insert(Equipment equipment);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Equipment equipment);

    /**
     * 根据ID查询
     */
    Equipment selectById(Integer id);

    /**
     * 查询所有
     */
    List<Equipment> selectAll(Equipment equipment);

}