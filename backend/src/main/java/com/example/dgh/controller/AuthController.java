package com.example.dgh.controller;

import com.example.dgh.dto.LoginCodeRequest;
import com.example.dgh.dto.LoginPasswordRequest;
import com.example.dgh.dto.LoginResponse;
import com.example.dgh.dto.RegisterRequest;
import com.example.dgh.dto.SendLoginCodeRequest;
import com.example.dgh.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login/password")
    public LoginResponse loginPassword(@Valid @RequestBody LoginPasswordRequest request) {
        return authService.loginWithPassword(request);
    }

    @PostMapping("/login/code/send")
    public void sendCode(@Valid @RequestBody SendLoginCodeRequest request) {
        authService.sendLoginCode(request);
    }

    @PostMapping("/login/code")
    public LoginResponse loginCode(@Valid @RequestBody LoginCodeRequest request) {
        return authService.loginWithCode(request);
    }
}
