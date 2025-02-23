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
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Coach coach) {
        coachService.add(coach);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        coachService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        coachService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Coach coach) {
        coachService.updateById(coach);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Coach coach = coachService.selectById(id);
        return Result.success(coach);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Coach coach ) {
        List<Coach> list = coachService.selectAll(coach);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Coach coach,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Coach> page = coachService.selectPage(coach, pageNum, pageSize);
        return Result.success(page);
    }

}
