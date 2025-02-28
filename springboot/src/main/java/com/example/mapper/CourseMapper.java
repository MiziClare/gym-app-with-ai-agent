package com.example.mapper;

import com.example.entity.Course;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作course相关数据接口
 */
public interface CourseMapper {

    /**
     * 新增
     */
    int insert(Course course);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Course course);

    /**
     * 根据ID查询
     */
    Course selectById(Integer id);

    /**
     * 查询所有
     */
    List<Course> selectAll(Course course);

    @Select("select course.*, coach.name as coachName from course " +
            "left join coach on course.coach_id = coach.id " +
            "order by id desc limit 4")
    List<Course> selectFour();
}