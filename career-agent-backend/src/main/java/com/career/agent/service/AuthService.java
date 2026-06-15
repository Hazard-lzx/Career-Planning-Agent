package com.career.agent.service;

import com.career.agent.dto.LoginRequest;
import com.career.agent.dto.RegisterRequest;

public interface AuthService {
    String login(LoginRequest req);
    String register(RegisterRequest req);
}
