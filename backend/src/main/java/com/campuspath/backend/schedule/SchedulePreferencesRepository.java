package com.campuspath.backend.schedule;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulePreferencesRepository extends JpaRepository<SchedulePreferences, Long> {

    Optional<SchedulePreferences> findByUserId(Long userId);
}