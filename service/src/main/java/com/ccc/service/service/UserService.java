package com.ccc.service.service;

import com.example.pojo.dto.UserLoginDTO;
import com.example.pojo.dto.UserRegisterDTO;
import com.example.pojo.dto.UserPageDTO;
import com.example.pojo.dto.UserUpdateDTO;
import com.example.pojo.entity.User;
import com.example.pojo.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    Result login(UserLoginDTO userLoginDTO);

    Result getUserById(Integer id);

    Result register(UserRegisterDTO userRegisterDTO);

    Result getPageUser(UserPageDTO userPageDTO);

    Result update(UserUpdateDTO user);

    Result delete(Integer id);

    String text();

    Result updateImg(MultipartFile file);

    Result tryRegister(UserRegisterDTO userRegisterDTO, String username);

    Result tryGetUser(UserPageDTO userPageDTO, List<User> result);

    Result tryUpdate(UserUpdateDTO userDto, User user);

    Result tryUpdateImg(MultipartFile file);
}
