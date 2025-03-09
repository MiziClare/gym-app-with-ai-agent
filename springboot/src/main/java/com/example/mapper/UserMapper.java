package com.example.mapper;

import com.example.entity.User;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * interface for operations on the user table
 */
public interface UserMapper {
    /**
     * Add
     */
    int insert(User user);

    /**
     * Delete
     */
    int deleteById(Integer id);

    /**
     * Update
     */
    int updateById(User user);

    /**
     * Query by ID
     */
    User selectById(Integer id);

    /**
     * Query all
     */
    List<User> selectAll(User user);

    @Select("select * from user where username = #{username}")
    User selectByUsername(String username);
}
