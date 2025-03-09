package com.example.controller;

import com.example.common.Result;
import com.example.entity.Eqreserve;
import com.example.service.EqreserveService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Equipment reservation table front-end operation interface
 **/
@RestController
@RequestMapping("/eqreserve")
public class EqreserveController {

    @Resource
    private EqreserveService eqreserveService;

    /**
     * Add
     */
    @PostMapping("/add")
    public Result add(@RequestBody Eqreserve eqreserve) {
        eqreserveService.add(eqreserve);
        return Result.success();
    }

    /**
     * Delete
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        eqreserveService.deleteById(id);
        return Result.success();
    }

    /**
     * Batch delete
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        eqreserveService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * Update
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Eqreserve eqreserve) {
        eqreserveService.updateById(eqreserve);
        return Result.success();
    }

    /**
     * Query by ID
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Eqreserve eqreserve = eqreserveService.selectById(id);
        return Result.success(eqreserve);
    }

    /**
     * Query all
     */
    @GetMapping("/selectAll")
    public Result selectAll(Eqreserve eqreserve ) {
        List<Eqreserve> list = eqreserveService.selectAll(eqreserve);
        return Result.success(list);
    }

    /**
     * Query by page
     */
    @GetMapping("/selectPage")
    public Result selectPage(Eqreserve eqreserve,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Eqreserve> page = eqreserveService.selectPage(eqreserve, pageNum, pageSize);
        return Result.success(page);
    }

}