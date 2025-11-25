package com.ccc.service.controller;

import com.ccc.service.service.CommonService;
import com.example.pojo.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private CommonService commonService;

    @GetMapping("/like/{urlId}")
    public Result like(@PathVariable Integer urlId) {
       return commonService.like(urlId);
    }

    @GetMapping("/unlike/{userId}")
    public Result subscribe(@PathVariable Integer userId) {
        return commonService.subscribe(userId);
    }

}
