package com.ccc.service.controller;

import com.ccc.service.service.CommonService;
import com.example.pojo.dto.UrlUploadDTO;
import com.example.pojo.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private CommonService commonService;

    @PostMapping("/upload")
    public Result upload(String introduce, String name,MultipartFile file) {return commonService.upload(introduce, name, file);}

    @GetMapping("/get/{id}")
    public Result get(@PathVariable Integer id) {return commonService.getUrl(id);}
}
