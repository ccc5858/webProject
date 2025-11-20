package com.ccc.common.utils;

import cn.hutool.jwt.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtils {

    public static String createToken(Map<String, Object> claims, long ttlMillis, String secretKey) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        long nowMillis = System.currentTimeMillis();
        long expMillis = nowMillis + ttlMillis;
        Date exp = new Date(expMillis);

        JwtBuilder jwtBuilder = Jwts.builder().setExpiration(exp).setClaims(claims).signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8));
        return jwtBuilder.compact();
    }

    public static Claims parseToken(String token, String secretKey) {
        Claims body = Jwts.parser().setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(token).getBody();
        return body;
    }
}
