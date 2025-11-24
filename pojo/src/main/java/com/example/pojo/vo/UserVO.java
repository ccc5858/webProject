package com.example.pojo.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {

    private String username;

    private int sex;

    private int age;

    private Set<String> urlList;
}
