package com.example.service;

import com.example.entity.Equipment;
import com.example.mapper.EquipmentMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

/**
 * Equipment information table business processing
 **/
@Service
public class EquipmentService {

    @Resource
    private EquipmentMapper equipmentMapper;

    /**
     * Add
     */
    public void add(Equipment equipment) {
        equipmentMapper.insert(equipment);
    }

    /**
     * Delete
     */
    public void deleteById(Integer id) {
        equipmentMapper.deleteById(id);
    }

    /**
     * Batch delete
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            equipmentMapper.deleteById(id);
        }
    }

    /**
     * Update
     */
    public void updateById(Equipment equipment) {
        equipmentMapper.updateById(equipment);
    }

    /**
     * Query by ID
     */
    public Equipment selectById(Integer id) {
        return equipmentMapper.selectById(id);
    }

    /**
     * Query all
     */
    public List<Equipment> selectAll(Equipment equipment) {
        return equipmentMapper.selectAll(equipment);
    }

    /**
     * Pagination query
     */
    public PageInfo<Equipment> selectPage(Equipment equipment, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Equipment> list = equipmentMapper.selectAll(equipment);
        return PageInfo.of(list);
    }

}