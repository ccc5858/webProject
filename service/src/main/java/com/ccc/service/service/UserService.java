package com.ccc.service.service;

import com.example.pojo.dto.UserLoginDTO;
import com.example.pojo.dto.UserRegisterDTO;
import com.example.pojo.dto.UserSelectPage;
import com.example.pojo.entity.User;
import com.example.pojo.result.Result;

public interface UserService {
    Result login(UserLoginDTO userLoginDTO);

    Result getUserById(Integer id);

    Result register(UserRegisterDTO userRegisterDTO);

    Result getUser(UserSelectPage userSelectPage);

    Result update(User user);

    Result delete(Integer id);
}
