package com.example.controller;

import com.example.common.Result;
import com.example.entity.Equipment;
import com.example.service.EquipmentService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Fitness equipment table front-end operation interface
 **/
@RestController
@RequestMapping("/equipment")
public class EquipmentController {

    @Resource
    private EquipmentService equipmentService;

    /**
     * Add
     */
    @PostMapping("/add")
    public Result add(@RequestBody Equipment equipment) {
        equipmentService.add(equipment);
        return Result.success();
    }

    /**
     * Delete
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        equipmentService.deleteById(id);
        return Result.success();
    }

    /**
     * Batch delete
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        equipmentService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * Update
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Equipment equipment) {
        equipmentService.updateById(equipment);
        return Result.success();
    }

    /**
     * Query by ID
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Equipment equipment = equipmentService.selectById(id);
        return Result.success(equipment);
    }

    /**
     * Query all
     */
    @GetMapping("/selectAll")
    public Result selectAll(Equipment equipment ) {
        List<Equipment> list = equipmentService.selectAll(equipment);
        return Result.success(list);
    }

    /**
     * Query by page
     */
    @GetMapping("/selectPage")
    public Result selectPage(Equipment equipment,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Equipment> page = equipmentService.selectPage(equipment, pageNum, pageSize);
        return Result.success(page);
    }

}