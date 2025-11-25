package com.ccc.service;

import com.ccc.service.annotation.logger;
import com.ccc.service.service.UserService;
import com.ccc.service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ServiceApplicationTests {

    @Autowired
    private UserService commonService;

    @Test
    void contextLoads() {
        System.out.println(commonService.text());
    }

}
