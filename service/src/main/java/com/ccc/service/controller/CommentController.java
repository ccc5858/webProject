package com.ccc.service.controller;

import com.ccc.service.service.CommentService;
import com.example.pojo.dto.CommentAddDTO;
import com.example.pojo.dto.CommentPageDTO;
import com.example.pojo.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评论
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/add")
    public Result add(@RequestBody CommentAddDTO commentAddDTO) {
        return commentService.add(commentAddDTO);
    }

    @PostMapping("/page/getPageComment")
    public Result getPageComment(@RequestBody CommentPageDTO commentPageDTO) {
        return commentService.getPageComment(commentPageDTO);
    }

    @GetMapping("/like/{commentId}")
    public Result like(@PathVariable Integer commentId) {
        return commentService.like(commentId);
    }

    @DeleteMapping("/delete/{commentId}")
    public Result delete(@PathVariable Integer commentId) {
        return commentService.delete(commentId);
    }
}
