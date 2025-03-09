package com.example.mapper;

import com.example.entity.Orders;

import java.util.List;

/**
 * Interface for operations on orders related data
 */
public interface OrdersMapper {

    /**
     * Add
     */
    int insert(Orders orders);

    /**
     * Delete
     */
    int deleteById(Integer id);

    /**
     * Update
     */
    int updateById(Orders orders);

    /**
     * Query by ID
     */
    Orders selectById(Integer id);

    /**
     * Query all
     */
    List<Orders> selectAll(Orders orders);

}