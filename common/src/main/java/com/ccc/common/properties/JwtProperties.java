package com.ccc.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "ccc.jwt")
@Component
@Data
public class JwtProperties {

    String secretKey = "nihaoccc";
    long ttl = 7200000;
    String tokenName = "token";

}
