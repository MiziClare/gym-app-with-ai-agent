package com.example.service;

import com.example.entity.CourseSchedule;
import com.example.mapper.CourseScheduleMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * course schedule service
 */
@Service
public class CourseScheduleService {

    @Resource
    private CourseScheduleMapper courseScheduleMapper;

    /**
     * add course schedule
     */
    public void add(CourseSchedule courseSchedule) {
        courseScheduleMapper.insert(courseSchedule);
    }

    /**
     * delete course schedule by id
     */
    public void deleteById(Integer id) {
        courseScheduleMapper.deleteById(id);
    }

    /**
     * delete course schedule by course id
     */
    public void deleteByCourseId(Integer courseId) {
        courseScheduleMapper.deleteByCourseId(courseId);
    }

    /**
     * delete course schedule by batch
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            courseScheduleMapper.deleteById(id);
        }
    }

    /**
     * update course schedule by id
     */
    public void updateById(CourseSchedule courseSchedule) {
        courseScheduleMapper.updateById(courseSchedule);
    }

    /**
     * select course schedule by id
     */
    public CourseSchedule selectById(Integer id) {
        return courseScheduleMapper.selectById(id);
    }

    /**
     * select course schedule by course id
     */
    public List<CourseSchedule> selectByCourseId(Integer courseId) {
        return courseScheduleMapper.selectByCourseId(courseId);
    }

    /**
     * select all course schedule
     */
    public List<CourseSchedule> selectAll(CourseSchedule courseSchedule) {
        return courseScheduleMapper.selectAll(courseSchedule);
    }

    /**
     * select course schedule by page
     */
    public PageInfo<CourseSchedule> selectPage(CourseSchedule courseSchedule, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<CourseSchedule> list = courseScheduleMapper.selectAll(courseSchedule);
        return PageInfo.of(list);
    }
}
