package com.example.pojo.entity;

import cn.hutool.core.date.DateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Url {

    private Integer id;

    private String url;

    private Integer userId;

    private DateTime uploadTime;

    private String introduce;

    private String name;

    private Integer version;
}
