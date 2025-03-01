package com.example.mapper;

import com.example.entity.Eqreserve;
import java.util.List;

/**
 * 操作eqreserve相关数据接口
 */
public interface EqreserveMapper {

    /**
     * 新增
     */
    int insert(Eqreserve eqreserve);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Eqreserve eqreserve);

    /**
     * 根据ID查询
     */
    Eqreserve selectById(Integer id);

    /**
     * 查询所有
     */
    List<Eqreserve> selectAll(Eqreserve eqreserve);

}