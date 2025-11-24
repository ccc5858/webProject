package com.ccc.service.service;

import com.example.pojo.dto.UrlUploadDTO;
import com.example.pojo.result.Result;
import org.springframework.web.multipart.MultipartFile;

public interface CommonService {
    Result upload(String introduce, String name, MultipartFile file);

    Result getUrl(Integer id);
}
