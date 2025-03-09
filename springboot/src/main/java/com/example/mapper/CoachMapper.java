package com.example.mapper;

import com.example.entity.Coach;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface CoachMapper {

    /**
     * Add
     */
    int insert(Coach coach);

    /**
     * Delete
     */
    int deleteById(Integer id);

    /**
     * Update
     */
    int updateById(Coach coach);

    /**
     * Query by ID
     */
    Coach selectById(Integer id);

    /**
     * Query all
     */
    List<Coach> selectAll(Coach coach);

    @Select("select * from coach where username = #{username}")
    Coach selectByUsername(String username);
}
