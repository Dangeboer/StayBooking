package com.laioffer.staybooking.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

// 这段代码是一个 JWT 处理类（JwtHandler），它用于生成和解析 JWT
// 代利用 io.jsonwebtoken 库实现了生成和解析 JWT 的功能码
@Component
public class JwtHandler {
    final Key signingKey; // 用于签名 JWT 的密钥。这个密钥用来确保 JWT 的有效性和安全性

    // @Value("${staybooking.jwt.secret-key}")：注入应用程序配置文件中的密钥
    // （通常是 application.yml 文件中的 staybooking.jwt.secret-key 配置项）
    public JwtHandler(@Value("${staybooking.jwt.secret-key}") String secretKey) {
        byte[] bytes = Base64.getDecoder().decode(secretKey); // 对密钥进行 Base64 解码
        signingKey = Keys.hmacShaKeyFor(bytes); // 生成一个 HMAC（哈希消息认证码）密钥，用于签名 JWT
    }

    // 用于解析 JWT 并获取用户名（即 subject）
    // 方法链（Method Chaining）
    // 在方法链中，每个方法执行时会返回一个对象，你可以继续调用下一个方法。
    // 每次调用方法时，返回的对象会提供新的方法可以继续执行。最后返回的就是 链式调用中最后执行的那个方法的返回值
    public String parsedUsername(String token) {
        return Jwts.parserBuilder() // 创建一个 JWT 解析器构建器
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token) // 解析传入的 JWT（token），并返回 Claims（声明），即 JWT 的负载部分
                .getBody()
                .getSubject(); // 从 JWT 的负载中提取 subject，通常用于存储用户名或用户 ID
    }

    // 用于生成一个 JWT，并将用户名（username）作为 subject 存入其中
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // 设置 JWT 的 subject
                .setIssuedAt(new Date(System.currentTimeMillis())) // 设置 JWT 的签发时间（当前时间）
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 设置 JWT 的过期时间，这里设置为签发后 24 小时
                .signWith(signingKey, SignatureAlgorithm.HS256) // 使用 HMACSHA256 算法和签名密钥进行签名
                .compact(); // 生成 JWT 字符串
    }
}
