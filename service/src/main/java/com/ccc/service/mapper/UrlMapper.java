package com.ccc.service.mapper;

import com.example.pojo.entity.Url;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UrlMapper {

    void insert(Integer userId, String url, LocalDateTime uploadTime, String introduce, String name);

    @Select("select * from url where url = #{url}")
    Url getByUrl(String url);

    @Select("select * from url where id = #{id}")
    Url getById(Integer id);

    int update(Url url);

    @Select("delete from url where id = #{id}")
    void delete(int i);

    Page<Url> getByPage(String introduce);
}
