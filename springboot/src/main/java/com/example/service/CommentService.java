package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Comment;
import com.example.entity.User;
import com.example.mapper.CommentMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评价信息表业务处理
 **/
@Service
public class CommentService {

    @Resource
    private CommentMapper commentMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * 新增
     */
    public void add(Comment comment) {
        comment.setTime(DateUtil.now()); // Set the current time as the time of the comment
        commentMapper.insert(comment);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        commentMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            commentMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(Comment comment) {
        commentMapper.updateById(comment);
    }

    /**
     * 根据ID查询
     */
    public Comment selectById(Integer id) {
        return commentMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Comment> selectAll(Comment comment) {
        List<Comment> list = commentMapper.selectAll(comment);
        for (Comment dbComment : list) {
            User user = userMapper.selectById(dbComment.getUserId());
            if (ObjectUtil.isNotEmpty(user)) {
                dbComment.setUserAvatar(user.getAvatar());
                dbComment.setUserName(user.getName());
            }
        }
        return list;
    }

    /**
     * 分页查询
     */
    public PageInfo<Comment> selectPage(Comment comment, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Comment> list = commentMapper.selectAll(comment);
        return PageInfo.of(list);
    }

}