package com.example.mapper;

import com.example.entity.Admin;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * interface for operations on the admin table
*/
public interface AdminMapper {

    /**
      * Add
    */
    int insert(Admin admin);

    /**
      * Delete
    */
    int deleteById(Integer id);

    /**
      * Update
    */
    int updateById(Admin admin);

    /**
      * Query by ID
    */
    Admin selectById(Integer id);

    /**
      * Query all
    */
    List<Admin> selectAll(Admin admin);

    @Select("select * from admin where username = #{username}")
    Admin selectByUsername(String username);
}