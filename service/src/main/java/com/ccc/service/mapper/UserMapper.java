package com.ccc.service.mapper;

import com.example.pojo.entity.User;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    Page<User> getUser(User user);

    @Select("select * from user where username = #{username}")
    User getByName(String username);

    @Select("select * from user where id = #{id}")
    User getById(Integer id);

    @Insert("insert into user (username, password, sex, age) values (#{username}, #{password}, #{sex}, #{age})")
    void insert(User user);

    @Delete("delete from user where id = #{id}")
    void delete(Integer id);

    int update(User user);

    @Insert("update user set img = #{url} where id = #{currentUser}")
    void updateImg(Integer currentUser, String img);
}
