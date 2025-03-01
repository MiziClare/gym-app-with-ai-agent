package com.example.controller;

import com.example.common.Result;
import com.example.entity.Eqreserve;
import com.example.service.EqreserveService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 器材预约表前端操作接口
 **/
@RestController
@RequestMapping("/eqreserve")
public class EqreserveController {

    @Resource
    private EqreserveService eqreserveService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Eqreserve eqreserve) {
        eqreserveService.add(eqreserve);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        eqreserveService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        eqreserveService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Eqreserve eqreserve) {
        eqreserveService.updateById(eqreserve);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Eqreserve eqreserve = eqreserveService.selectById(id);
        return Result.success(eqreserve);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Eqreserve eqreserve ) {
        List<Eqreserve> list = eqreserveService.selectAll(eqreserve);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Eqreserve eqreserve,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Eqreserve> page = eqreserveService.selectPage(eqreserve, pageNum, pageSize);
        return Result.success(page);
    }

}