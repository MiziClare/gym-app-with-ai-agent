package com.example.controller;

import com.example.common.Result;
import com.example.entity.Experience;
import com.example.service.ExperienceService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

/**
 * Fitness experience table front-end operation interface
 **/
@RestController
@RequestMapping("/experience")
public class ExperienceController {

    @Resource
    private ExperienceService experienceService;

    /**
     * Add
     */
    @PostMapping("/add")
    public Result add(@RequestBody Experience experience) {
        experienceService.add(experience);
        return Result.success();
    }

    /**
     * Delete
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        experienceService.deleteById(id);
        return Result.success();
    }

    /**
     * Batch delete
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        experienceService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * Update
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Experience experience) {
        experienceService.updateById(experience);
        return Result.success();
    }

    /**
     * Query by ID
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Experience experience = experienceService.selectById(id);
        return Result.success(experience);
    }

    /**
     * Query all
     */
    @GetMapping("/selectAll")
    public Result selectAll(Experience experience ) {
        List<Experience> list = experienceService.selectAll(experience);
        return Result.success(list);
    }

    /**
     * Query by page
     */
    @GetMapping("/selectPage")
    public Result selectPage(Experience experience,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Experience> page = experienceService.selectPage(experience, pageNum, pageSize);
        return Result.success(page);
    }

}