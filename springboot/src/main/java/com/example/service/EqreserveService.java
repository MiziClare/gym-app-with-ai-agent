package com.example.service;

import com.example.entity.Eqreserve;
import com.example.mapper.EqreserveMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * Equipment reservation table business processing
 **/
@Service
public class EqreserveService {

    @Resource
    private EqreserveMapper eqreserveMapper;

    /**
     * Add
     */
    public void add(Eqreserve eqreserve) {
        eqreserveMapper.insert(eqreserve);
    }

    /**
     * Delete
     */
    public void deleteById(Integer id) {
        eqreserveMapper.deleteById(id);
    }

    /**
     * Batch delete
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            eqreserveMapper.deleteById(id);
        }
    }

    /**
     * Update
     */
    public void updateById(Eqreserve eqreserve) {
        eqreserveMapper.updateById(eqreserve);
    }

    /**
     * Query by ID
     */
    public Eqreserve selectById(Integer id) {
        return eqreserveMapper.selectById(id);
    }

    /**
     * Query all
     */
    public List<Eqreserve> selectAll(Eqreserve eqreserve) {
        return eqreserveMapper.selectAll(eqreserve);
    }

    /**
     * Pagination query
     */
    public PageInfo<Eqreserve> selectPage(Eqreserve eqreserve, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Eqreserve> list = eqreserveMapper.selectAll(eqreserve);
        return PageInfo.of(list);
    }

}