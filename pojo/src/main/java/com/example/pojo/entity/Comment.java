package com.example.pojo.entity;

import cn.hutool.core.date.DateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {
    private Integer id;

    private Integer userId;

    private Integer urlId;

    private Integer currUserId;

    private String message;

    private LocalDateTime createTime;

    private Integer likeCount;

    private Integer version;

    private Integer parentId;
}
