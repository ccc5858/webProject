package com.ccc.service.service;

import com.example.pojo.dto.UserLoginDTO;
import com.example.pojo.dto.UserRegisterDTO;
import com.example.pojo.dto.UserPageDTO;
import com.example.pojo.dto.UserUpdateDTO;
import com.example.pojo.result.Result;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    Result login(UserLoginDTO userLoginDTO);

    Result getUserById(Integer id);

    Result register(UserRegisterDTO userRegisterDTO);

    Result getUser(UserPageDTO userPageDTO);

    Result update(UserUpdateDTO user);

    Result delete(Integer id);

    String text();

    Result updateImg(MultipartFile file);
}
