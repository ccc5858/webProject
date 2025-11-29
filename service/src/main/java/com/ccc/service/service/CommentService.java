package com.ccc.service.service;

import com.example.pojo.dto.CommentAddDTO;
import com.example.pojo.dto.CommentPageDTO;
import com.example.pojo.result.Result;

public interface CommentService {
    Result add(CommentAddDTO commentAddDTO);

    Result getPageComment(CommentPageDTO commentPageDTO);

    Result like(Integer id);

    Result delete(Integer id);
}
