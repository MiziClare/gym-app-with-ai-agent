package com.example.service;

import com.example.entity.Eqreserve;
import com.example.mapper.EqreserveMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * 器材预约表业务处理
 **/
@Service
public class EqreserveService {

    @Resource
    private EqreserveMapper eqreserveMapper;

    /**
     * 新增
     */
    public void add(Eqreserve eqreserve) {
        eqreserveMapper.insert(eqreserve);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        eqreserveMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            eqreserveMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Eqreserve eqreserve) {
        eqreserveMapper.updateById(eqreserve);
    }

    /**
     * 根据ID查询
     */
    public Eqreserve selectById(Integer id) {
        return eqreserveMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Eqreserve> selectAll(Eqreserve eqreserve) {
        return eqreserveMapper.selectAll(eqreserve);
    }

    /**
     * 分页查询
     */
    public PageInfo<Eqreserve> selectPage(Eqreserve eqreserve, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Eqreserve> list = eqreserveMapper.selectAll(eqreserve);
        return PageInfo.of(list);
    }

}