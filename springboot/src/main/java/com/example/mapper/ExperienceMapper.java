package com.example.mapper;

import com.example.entity.Experience;

import java.util.List;

/**
 * Interface for operations on experience related data
 */
public interface ExperienceMapper {

    /**
     * Add
     */
    int insert(Experience experience);

    /**
     * Delete
     */
    int deleteById(Integer id);

    /**
     * Update
     */
    int updateById(Experience experience);

    /**
     * Query by ID
     */
    Experience selectById(Integer id);

    /**
     * Query all
     */
    List<Experience> selectAll(Experience experience);

}