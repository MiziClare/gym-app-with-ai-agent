package com.example.mapper;

import com.example.entity.CourseSchedule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * course schedule data access interface
 */
public interface CourseScheduleMapper {

    /**
     * add course schedule
     */
    int insert(CourseSchedule courseSchedule);

    /**
     * delete course schedule by id
     */
    int deleteById(Integer id);

    /**
     * delete course schedule by course id
     */
    int deleteByCourseId(Integer courseId);

    /**
     * update course schedule by id
     */
    int updateById(CourseSchedule courseSchedule);

    /**
     * select course schedule by id
     */
    CourseSchedule selectById(Integer id);

    /**
     * select course schedule by course id
     */
    List<CourseSchedule> selectByCourseId(Integer courseId);

    /**
     * select all course schedule
     */
    List<CourseSchedule> selectAll(CourseSchedule courseSchedule);
}
