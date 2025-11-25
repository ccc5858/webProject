package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {

    private String username;

    private String password;

    private String confirmPassword;

    private String oldPassword;

    private int sex;

    private int age;

    private String img;
}
