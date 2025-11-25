package com.ccc.service.service;

import com.example.pojo.dto.UrlPageDTO;
import com.example.pojo.dto.UrlUpdateDTO;
import com.example.pojo.result.Result;
import org.springframework.web.multipart.MultipartFile;

public interface UrlService {
    Result upload(String introduce, String name, MultipartFile file);

    Result getUrl(Integer id);

    Result update(UrlUpdateDTO urlUpdateDTO);

    Result delete(Integer id);

    Result pageSelect(UrlPageDTO urlPageDTO);
}
