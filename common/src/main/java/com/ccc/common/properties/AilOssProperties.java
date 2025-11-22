package com.ccc.common.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "ccc.alioss")
@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AilOssProperties {

    private String endpoint = "oss-cn-beijing.aliyuncs.com";
    private String accessKeyId = "LTAI5t7kbAR5QX3toWwyHyYb";
    private String accessKeySecret = "qkFtKoghOStRbeNeYYXAl7G6yWeW3a";
    private String bucketName = "java-web-ccc-project";

}
