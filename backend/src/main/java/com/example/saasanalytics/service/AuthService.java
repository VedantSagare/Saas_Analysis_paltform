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

        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole("ROLE_USER");

        return Optional.of(userRepository.save(user));
    }

    public String login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        return jwtUtil.generateToken(req.getUsername());
    }
}
