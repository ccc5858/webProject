package com.ccc.common.handle;

import com.example.pojo.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//@RestControllerAdvice
//@Slf4j
//public class GlobalHandler {
//
//    @ExceptionHandler(Exception.class)
//    public Result error(Exception e) {
//        log.error(e.getMessage());
//        return Result.error(e.getMessage());
//    }
//}
