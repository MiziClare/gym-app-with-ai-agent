package com.example.service;

import com.example.entity.Course;
import com.example.mapper.CourseMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Course information table business processing
 **/
@Service
public class CourseService {

    @Resource
    private CourseMapper courseMapper;

    /**
     * Add
     */
    public void add(Course course) {
        courseMapper.insert(course);
    }

    /**
     * Delete
     */
    public void deleteById(Integer id) {
        courseMapper.deleteById(id);
    }

    /**
     * Batch delete
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            courseMapper.deleteById(id);
        }
    }

    /**
     * Update
     */
    public void updateById(Course course) {
        courseMapper.updateById(course);
    }

    /**
     * Query by ID
     */
    public Course selectById(Integer id) {
        return courseMapper.selectById(id);
    }

    /**
     * Query all
     */
    public List<Course> selectAll(Course course) {
        return courseMapper.selectAll(course);
    }

    /**
     * Pagination query
     */
    public PageInfo<Course> selectPage(Course course, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Course> list = courseMapper.selectAll(course);
        return PageInfo.of(list);
    }

    public List<Course> selectFour() {
        return courseMapper.selectFour();
    }
}