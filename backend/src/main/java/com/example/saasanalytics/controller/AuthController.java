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
            var resp = authService.login(req);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody com.example.saasanalytics.auth.dto.RefreshRequest req) {
        try {
            var resp = authService.refreshAccessToken(req.getRefreshToken());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body("Invalid or expired refresh token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody com.example.saasanalytics.auth.dto.RefreshRequest req) {
        authService.logout(req.getRefreshToken());
        return ResponseEntity.ok().build();
    }
}
