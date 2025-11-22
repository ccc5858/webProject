package com.ccc.service.mapper;

import com.example.pojo.entity.UserWithUrl;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface CommonMapper {

    @Insert("insert into upload(user_id, url, upload_time) values(#{userId}, #{url}, #{uploadTime})")
    void insert(Integer userId, String url, LocalDateTime uploadTime);

    @Select("select * from upload where url = #{url}")
    UserWithUrl getByUrl(String url);
}
