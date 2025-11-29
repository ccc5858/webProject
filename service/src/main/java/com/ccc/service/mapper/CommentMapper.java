package com.ccc.service.mapper;

import com.example.pojo.dto.CommentAddDTO;
import com.example.pojo.dto.CommentPageDTO;
import com.example.pojo.entity.Comment;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CommentMapper {
    void add(CommentAddDTO commentAddDTO, Integer id, LocalDateTime now);

    Page<Comment> getPage(CommentPageDTO commentPageDTO);

    @Select("select * from comment where id = #{id}")
    Comment getById(Integer id);

    int update(Comment byId);

    @Select("select * from comment where url_id = #{id}")
    List<Comment> getByUrlId(Integer id);

    void deleteList(List<Comment> comments);

    @Select("delete from comment where id = #{id}")
    void deleteById(Integer id);
}
