package com.example.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentPageDTO {

    private Integer pageNum;

    private Integer page;

    private Integer parentId;

    private Integer urlId;
}
