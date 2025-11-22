package com.ccc.service.service;

import com.example.pojo.result.Result;
import org.springframework.web.multipart.MultipartFile;

public interface CommonService {
    Result upload(MultipartFile file);

    Result getUrl(Integer id);
}
