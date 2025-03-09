package com.example.mapper;

import com.example.entity.Equipment;

import java.util.List;

/**
 * Interface for operations on equipment related data
 */
public interface EquipmentMapper {

    /**
     * Add
     */
    int insert(Equipment equipment);

    /**
     * Delete
     */
    int deleteById(Integer id);

    /**
     * Update
     */
    int updateById(Equipment equipment);

    /**
     * Query by ID
     */
    Equipment selectById(Integer id);

    /**
     * Query all
     */
    List<Equipment> selectAll(Equipment equipment);

}