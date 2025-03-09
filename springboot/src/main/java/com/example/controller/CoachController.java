package com.example.controller;

import com.example.common.Result;
import com.example.entity.Coach;
import com.example.service.CoachService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/coach")
public class CoachController {

    @Resource
    private CoachService coachService;

    /**
     * Add
     */
    @PostMapping("/add")
    public Result add(@RequestBody Coach coach) {
        coachService.add(coach);
        return Result.success();
    }

    /**
     * Delete
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        coachService.deleteById(id);
        return Result.success();
    }

    /**
     * Batch delete
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        coachService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * Update
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Coach coach) {
        coachService.updateById(coach);
        return Result.success();
    }

    /**
     * Query by ID
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Coach coach = coachService.selectById(id);
        return Result.success(coach);
    }

    /**
     * Query all
     */
    @GetMapping("/selectAll")
    public Result selectAll(Coach coach ) {
        List<Coach> list = coachService.selectAll(coach);
        return Result.success(list);
    }

    /**
     * Query by page
     */
    @GetMapping("/selectPage")
    public Result selectPage(Coach coach,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Coach> page = coachService.selectPage(coach, pageNum, pageSize);
        return Result.success(page);
    }

}
