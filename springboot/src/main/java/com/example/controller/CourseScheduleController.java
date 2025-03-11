package com.example.controller;

import com.example.common.Result;
import com.example.entity.CourseSchedule;
import com.example.service.CourseScheduleService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * course schedule
 */
@RestController
@RequestMapping("/courseSchedule")
public class CourseScheduleController {

    @Resource
    private CourseScheduleService courseScheduleService;

    /**
     * add course schedule
     */
    @PostMapping("/add")
    public Result add(@RequestBody CourseSchedule courseSchedule) {
        courseScheduleService.add(courseSchedule);
        return Result.success();
    }

    /**
     * delete course schedule by id
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        courseScheduleService.deleteById(id);
        return Result.success();
    }

    /**
     * delete course schedule by course id
     */
    @DeleteMapping("/deleteByCourseId/{courseId}")
    public Result deleteByCourseId(@PathVariable Integer courseId) {
        courseScheduleService.deleteByCourseId(courseId);
        return Result.success();
    }

    /**
     * delete course schedule by batch
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        courseScheduleService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * update course schedule by id
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody CourseSchedule courseSchedule) {
        courseScheduleService.updateById(courseSchedule);
        return Result.success();
    }

    /**
     * select course schedule by id
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        CourseSchedule courseSchedule = courseScheduleService.selectById(id);
        return Result.success(courseSchedule);
    }

    /**
     * select course schedule by course id
     */
    @GetMapping("/selectByCourseId/{courseId}")
    public Result selectByCourseId(@PathVariable Integer courseId) {
        List<CourseSchedule> list = courseScheduleService.selectByCourseId(courseId);
        return Result.success(list);
    }

    /**
     * select all course schedule
     */
    @GetMapping("/selectAll")
    public Result selectAll(CourseSchedule courseSchedule) {
        List<CourseSchedule> list = courseScheduleService.selectAll(courseSchedule);
        return Result.success(list);
    }

    /**
     * select course schedule by page
     */
    @GetMapping("/selectPage")
    public Result selectPage(CourseSchedule courseSchedule,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<CourseSchedule> page = courseScheduleService.selectPage(courseSchedule, pageNum, pageSize);
        return Result.success(page);
    }
}
