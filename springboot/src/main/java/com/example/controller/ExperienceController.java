package com.example.controller;

import com.example.common.Result;
import com.example.entity.Experience;
import com.example.service.ExperienceService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

/**
 * 健身经验表前端操作接口
 **/
@RestController
@RequestMapping("/experience")
public class ExperienceController {

    @Resource
    private ExperienceService experienceService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Experience experience) {
        experienceService.add(experience);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        experienceService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        experienceService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Experience experience) {
        experienceService.updateById(experience);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Experience experience = experienceService.selectById(id);
        return Result.success(experience);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Experience experience ) {
        List<Experience> list = experienceService.selectAll(experience);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Experience experience,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Experience> page = experienceService.selectPage(experience, pageNum, pageSize);
        return Result.success(page);
    }

}