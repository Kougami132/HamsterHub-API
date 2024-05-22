package com.hamsterhub.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.time.*;
import java.util.Date;
import java.util.UUID;

public class JwtUtil {
    private static String secretKey = "j132o132k132e132r";

    public static void setSecretKey(String key) {
        secretKey = key;
    }

    public static String createToken(Long accountId, String username, Integer expiryDay) {
        JwtBuilder jwtBuilder = Jwts.builder();
        String token = jwtBuilder
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .claim("username", username)
                .claim("createTime", System.currentTimeMillis())
                .setSubject("token")
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * expiryDay))
                .setId(UUID.randomUUID().toString())
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        return token;
    }

    public static Boolean checkToken(String token) {
        if (StringUtil.isBlank(token)) return false;
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public static String getUsername(String token) {
        Claims body = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        return body.get("username").toString();
    }

    public static LocalDateTime getCreateTime(String token) {
        Claims body = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        long createTime = Long.valueOf(body.get("createTime").toString());
        LocalDateTime localDateTime = Instant.ofEpochMilli(createTime).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
        return localDateTime;
    }

    public static LocalDateTime getExpiryTime(String token) {
        Date date = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getExpiration();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDateTime;
    }

}
