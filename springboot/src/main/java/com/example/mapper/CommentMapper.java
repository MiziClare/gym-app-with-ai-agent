package com.example.mapper;

import com.example.entity.Comment;

import java.util.List;

/**
 * Interface for operations on comment related data
 */
public interface CommentMapper {

    /**
     * Add
     */
    int insert(Comment comment);

    /**
     * Delete
     */
    int deleteById(Integer id);

    /**
     * Update
     */
    int updateById(Comment comment);

    /**
     * Query by ID
     */
    Comment selectById(Integer id);

    /**
     * Query all
     */
    List<Comment> selectAll(Comment comment);

}