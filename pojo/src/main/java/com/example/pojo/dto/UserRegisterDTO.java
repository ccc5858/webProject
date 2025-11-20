package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDTO {

    private String username;

    private String newPassword;

    private String confirmPassword;

    private int sex;

    private int age;
}
