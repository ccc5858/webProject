package com.ccc.service.mapper;

import com.example.pojo.entity.Url;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface CommonMapper {

    void insert(Integer userId, String url, LocalDateTime uploadTime, String introduce, String name);

    @Select("select * from url where url = #{url}")
    Url getByUrl(String url);

    @Select("select * from url where id = #{id}")
    Url getById(Integer id);
}
