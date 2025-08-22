package com.laioffer.staybooking.controller;

import com.laioffer.staybooking.service.AuthenticationService;
import com.laioffer.staybooking.model.LoginRequest;
import com.laioffer.staybooking.model.LoginResponse;
import com.laioffer.staybooking.model.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // 注册
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody RegisterRequest body) {
        authenticationService.register(body.username(), body.password(), body.role());
    }

    // 登陆
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest body) {
        String token = authenticationService.login(body.username(), body.password());
        return new LoginResponse(token); // 返回token给前端，也就是JWT
    }
}
