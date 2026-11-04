package com.campuspath.backend.schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleEventRepository extends JpaRepository<ScheduleEvent, Long> {

    List<ScheduleEvent> findByUserIdOrderByEventDateAscStartTimeAsc(Integer userId);

    List<ScheduleEvent> findByUserIdAndEventDateBetweenOrderByEventDateAscStartTimeAsc(
            Integer userId, LocalDate from, LocalDate to);

    Optional<ScheduleEvent> findByIdAndUserId(Long id, Integer userId);
}