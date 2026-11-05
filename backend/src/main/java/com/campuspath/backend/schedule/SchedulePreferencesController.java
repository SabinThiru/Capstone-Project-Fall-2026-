package com.campuspath.backend.schedule;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.campuspath.backend.schedule.ScheduleDtos.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/me/schedule-preferences")
public class SchedulePreferencesController {

    private final ScheduleService schedule;

    public SchedulePreferencesController(ScheduleService schedule) {
        this.schedule = schedule;
    }

    @GetMapping
    public PreferencesResponse get(@AuthenticationPrincipal Jwt jwt) {
        return schedule.getPreferences(jwt.getSubject());
    }

    @PutMapping
    public PreferencesResponse save(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PreferencesRequest body) {
        return schedule.savePreferences(jwt.getSubject(), body);
    }
}