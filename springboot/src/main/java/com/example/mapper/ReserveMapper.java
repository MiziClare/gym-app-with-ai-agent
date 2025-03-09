package com.example.mapper;

import com.example.entity.Reserve;

import java.util.List;

/**
 * Interface for operations on reserve related data
 */
public interface ReserveMapper {

    /**
     * Add
     */
    int insert(Reserve reserve);

    /**
     * Delete
     */
    int deleteById(Integer id);

    /**
     * Update
     */
    int updateById(Reserve reserve);

    /**
     * Query by ID
     */
    Reserve selectById(Integer id);

    /**
     * Query all
     */
    List<Reserve> selectAll(Reserve reserve);

}