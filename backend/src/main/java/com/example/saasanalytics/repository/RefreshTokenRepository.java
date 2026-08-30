package com.example.saasanalytics.repository;

import com.example.saasanalytics.domain.RefreshToken;
import com.example.saasanalytics.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    int deleteByUser(User user);
}
