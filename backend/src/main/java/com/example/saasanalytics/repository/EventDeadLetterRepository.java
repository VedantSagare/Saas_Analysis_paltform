package com.example.saasanalytics.repository;

import com.example.saasanalytics.domain.EventDeadLetter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventDeadLetterRepository extends JpaRepository<EventDeadLetter, Long> {
}
