package com.campuspath.backend.schedule;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyCommitmentRepository extends JpaRepository<WeeklyCommitment, Long> {

    List<WeeklyCommitment> findByUserIdOrderByDayOfWeekAscStartTimeAsc(Integer userId);

    Optional<WeeklyCommitment> findByIdAndUserId(Long id, Integer userId);
}