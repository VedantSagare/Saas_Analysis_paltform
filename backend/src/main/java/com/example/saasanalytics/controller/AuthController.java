package com.example.saasanalytics.controller;

import com.example.saasanalytics.auth.dto.AuthResponse;
import com.example.saasanalytics.auth.dto.LoginRequest;
import com.example.saasanalytics.auth.dto.RegisterRequest;
import com.example.saasanalytics.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req)
                .map(u -> ResponseEntity.ok().build())
                .orElseGet(() -> ResponseEntity.badRequest().body("Username or email already in use"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            String token = authService.login(req);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}
