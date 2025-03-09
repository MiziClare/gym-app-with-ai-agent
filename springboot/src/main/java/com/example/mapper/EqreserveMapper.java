package com.example.mapper;

import com.example.entity.Eqreserve;
import java.util.List;

/**
 * Interface for operations on eqreserve related data
 */
public interface EqreserveMapper {

    /**
     * Add
     */
    int insert(Eqreserve eqreserve);

    /**
     * Delete
     */
    int deleteById(Integer id);

    /**
     * Update
     */
    int updateById(Eqreserve eqreserve);

    /**
     * Query by ID
     */
    Eqreserve selectById(Integer id);

    /**
     * Query all
     */
    List<Eqreserve> selectAll(Eqreserve eqreserve);

}