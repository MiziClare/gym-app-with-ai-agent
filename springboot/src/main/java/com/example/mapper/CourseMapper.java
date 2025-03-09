package com.example.mapper;

import com.example.entity.Course;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Interface for operations on course related data
 */
public interface CourseMapper {

    /**
     * Add
     */
    int insert(Course course);

    /**
     * Delete
     */
    int deleteById(Integer id);

    /**
     * Update
     */
    int updateById(Course course);

    /**
     * Query by ID
     */
    Course selectById(Integer id);

    /**
     * Query all
     */
    List<Course> selectAll(Course course);

    @Select("select course.*, coach.name as coachName from course " +
            "left join coach on course.coach_id = coach.id " +
            "order by id desc limit 4")
    List<Course> selectFour();
}