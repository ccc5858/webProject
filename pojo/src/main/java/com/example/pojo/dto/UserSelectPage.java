package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSelectPage {

    private Integer pageNum;

    private Integer pageSize;

    private String username;

}
