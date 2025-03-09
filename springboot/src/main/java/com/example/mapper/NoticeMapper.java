package com.example.mapper;

import com.example.entity.Notice;
import java.util.List;

/**
 * Interface for operations on notice related data
*/
public interface NoticeMapper {

    /**
      * Add
    */
    int insert(Notice notice);

    /**
      * Delete
    */
    int deleteById(Integer id);

    /**
      * Update
    */
    int updateById(Notice notice);

    /**
      * Query by ID
    */
    Notice selectById(Integer id);

    /**
      * Query all
    */
    List<Notice> selectAll(Notice notice);

}