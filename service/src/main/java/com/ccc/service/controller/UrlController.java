package com.ccc.service.controller;

import com.ccc.service.service.UrlService;
import com.example.pojo.dto.UrlPageDTO;
import com.example.pojo.dto.UrlUpdateDTO;
import com.example.pojo.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 资源上传
 */
@RestController
@RequestMapping("/url")
public class UrlController {

    @Autowired
    private UrlService urlService;

    @PostMapping("/upload")
    public Result upload(String introduce, String name, MultipartFile file) {return urlService.upload(introduce, name, file);}

    @GetMapping("/get/{id}")
    public Result get(@PathVariable Integer id) {return urlService.getUrl(id);}

    @PostMapping("/update")
    public Result update(@RequestBody UrlUpdateDTO urlUpdateDTO) {return urlService.update(urlUpdateDTO);}

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {return urlService.delete(id);}

    @PostMapping("/page/select")
    public Result pageSelect(@RequestBody UrlPageDTO urlPageDTO) {return urlService.pageSelect(urlPageDTO);}
}
