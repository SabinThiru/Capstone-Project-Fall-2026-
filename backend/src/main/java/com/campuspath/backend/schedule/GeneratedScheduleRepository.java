package com.campuspath.backend.schedule;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedScheduleRepository extends JpaRepository<GeneratedSchedule, Long> {

    List<GeneratedSchedule> findByUserIdAndTermIdOrderByIdAsc(Integer userId, Long termId);

    Optional<GeneratedSchedule> findByIdAndUserId(Long id, Integer userId);
}