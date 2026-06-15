package com.career.agent.controller;

import com.career.agent.common.Result;
import com.career.agent.dto.LoginRequest;
import com.career.agent.dto.RegisterRequest;
import com.career.agent.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginRequest req) {
        String token = authService.login(req);
        return Result.success(Map.of("token", token));
    }

    @PostMapping("/register")
    public Result<Map<String, String>> register(@RequestBody RegisterRequest req) {
        String token = authService.register(req);
        return Result.success(Map.of("token", token));
    }
}
