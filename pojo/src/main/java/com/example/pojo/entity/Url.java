package com.example.pojo.entity;

import cn.hutool.core.date.DateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Url {

    private Integer id;

    private String url;

    private Integer userId;

    private LocalDateTime uploadTime;

    private String introduce;

    private String name;

    private Integer version;

    private int likeCount;

}
