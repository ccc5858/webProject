package com.ccc.service.controller;

import com.ccc.service.service.UserService;
import com.example.pojo.dto.UserLoginDTO;
import com.example.pojo.dto.UserRegisterDTO;
import com.example.pojo.dto.UserSelectPage;
import com.example.pojo.entity.User;
import com.example.pojo.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody UserLoginDTO userLoginDTO) {
        return userService.login(userLoginDTO);
    }

    @GetMapping("/getUser/{id}")
    public Result getUser(@PathVariable Integer id) {
        return userService.getUserById(id);
    }

    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO) {
        return userService.register(userRegisterDTO);
    }

    @PostMapping("/getUser")
    public Result getUser(UserSelectPage userSelectPage) {
        return userService.getUser(userSelectPage);
    }

    @PostMapping("/update")
    public Result update(@RequestBody User user) {
        return userService.update(user);
    }

    @GetMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        return userService.delete(id);
    }
}
