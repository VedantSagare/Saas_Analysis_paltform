package com.example.saasanalytics.service;

import com.example.saasanalytics.auth.dto.LoginRequest;
import com.example.saasanalytics.auth.dto.RegisterRequest;
import com.example.saasanalytics.domain.User;
import com.example.saasanalytics.repository.UserRepository;
import com.example.saasanalytics.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public Optional<User> register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername()) || userRepository.existsByEmail(req.getEmail())) {
            return Optional.empty();
        }

        // Determine role: default to VIEWER. If caller is ADMIN and provided a valid role, honor it.
        String desired = req.getRole();
        String assignedRole = "ROLE_VIEWER";

        if (desired != null && !desired.isBlank()) {
            String normalized = desired.trim().toUpperCase();
            if (normalized.equals("ADMIN") || normalized.equals("ANALYST") || normalized.equals("VIEWER")) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                boolean callerIsAdmin = auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                if (callerIsAdmin) {
                    assignedRole = "ROLE_" + normalized;
                }
            }
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(assignedRole);

        return Optional.of(userRepository.save(user));
    }

    public String login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        return jwtUtil.generateToken(req.getUsername());
    }
}
