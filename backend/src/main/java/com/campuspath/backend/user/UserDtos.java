package com.campuspath.backend.user;

import com.campuspath.backend.university.UniversityResponse;

import jakarta.validation.constraints.*;

public final class UserDtos {

    private UserDtos() { }


    public record RegisterRequest(
            @NotNull(message = "University is required")
            Integer universityId,

            @NotBlank(message = "Full name is required")
            @Size(max = 200, message = "Full name must be at most 200 characters")
            String fullName,

            

            @NotBlank(message = "EUID is required")
            @Size(max = 50, message = "EUID must be at most 50 characters")
            String euid) { }


    public record UserResponse(Integer id, String email, String fullName, String euid,
                               UniversityResponse university) {
        static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getEuid(),
                    UniversityResponse.from(u.getUniversity()));
        }
    }

    public record SettingsResponse(String timezone, String theme, boolean notificationsEnabled,
                                   boolean emailRemindersEnabled, int defaultReminderMinutes) {
        static SettingsResponse from(UserSettings s) {
            return new SettingsResponse(s.getTimezone(), s.getTheme(), s.isNotificationsEnabled(),
                    s.isEmailRemindersEnabled(), s.getDefaultReminderMinutes());
        }
    }


    public record UpdateSettingsRequest(
            @NotBlank @Size(max = 50) String timezone,
            @NotBlank @Pattern(regexp = "light|dark|system", message = "Theme must be light, dark or system") String theme,
            @NotNull Boolean notificationsEnabled,
            @NotNull Boolean emailRemindersEnabled,
            @NotNull @Min(0) @Max(10080) Integer defaultReminderMinutes) { }
}