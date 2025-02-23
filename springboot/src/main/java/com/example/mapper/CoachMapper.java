package com.example.mapper;

import com.example.entity.Coach;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface CoachMapper {

    /**
     * 新增
     */
    int insert(Coach coach);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Coach coach);

    /**
     * 根据ID查询
     */
    Coach selectById(Integer id);

    /**
     * 查询所有
     */
    List<Coach> selectAll(Coach coach);

    @Select("select * from coach where username = #{username}")
    Coach selectByUsername(String username);
}
