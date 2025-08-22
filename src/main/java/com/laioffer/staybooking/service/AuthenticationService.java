package com.laioffer.staybooking.service;

import com.laioffer.staybooking.model.entity.UserEntity;
import com.laioffer.staybooking.exception.UserAlreadyExistException;
import com.laioffer.staybooking.model.UserRole;
import com.laioffer.staybooking.repository.UserRepository;
import com.laioffer.staybooking.security.JwtHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// 负责用户的注册和登录功能，整合了 Spring Security 的身份认证机制，并使用 JWT 进行身份令牌管理
@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtHandler jwtHandler;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            JwtHandler jwtHandler,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtHandler = jwtHandler;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public UserEntity register(String username, String password, UserRole role) throws UserAlreadyExistException {
        // 检查用户名是否已存在（防止重复注册）
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistException();
        }

        // 如果用户名不存在，使用 PasswordEncoder 对明文密码进行加密存储
        UserEntity userEntity = new UserEntity(null, username, passwordEncoder.encode(password), role);
        return userRepository.save(userEntity); // 创建 UserEntity 对象，并调用 userRepository.save(userEntity) 保存到数据库
    }

    // authenticationManager.authenticate() 负责验证用户名和密码：
    //      UsernamePasswordAuthenticationToken(username, password) 创建认证令牌。
    //      authenticationManager 会将这个令牌交给 AuthenticationProvider 进行身份验证（通常是 DaoAuthenticationProvider）。
    //      验证通过后，Spring Security 会在上下文中存储 SecurityContextHolder，表示用户已登录。
    // 认证通过后，调用 jwtHandler.generateToken(username) 生成 JWT 令牌，并返回给前端。

    // 总之就是生成一个JWT(String)，返回给前端
    public String login(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password)); // 创建认证令牌
        return jwtHandler.generateToken(username);
    }
}
