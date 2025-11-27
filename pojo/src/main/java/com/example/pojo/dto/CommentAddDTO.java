package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentAddDTO {

    private Integer userId;

    private Integer urlId;

    private String content;

    private Integer parentId;
}
