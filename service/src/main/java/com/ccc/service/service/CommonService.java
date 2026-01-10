package com.ccc.service.service;

import com.example.pojo.dto.CommentAddDTO;
import com.example.pojo.entity.Comment;
import com.example.pojo.result.Result;

public interface CommonService {
    Result like(Integer urlId);

    Result subscribe(Integer userId);

    Result tryLike(Integer urlId, Integer userId);

    Result tryUnlike(Integer urlId, Integer userId);

    Result trySubscribe(Integer userId, Integer currentUser);

    Result tryUnsubscribe(Integer userId, Integer currentUser);
}
