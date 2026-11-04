package com.campuspath.backend.schedule;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.*;

public final class ScheduleDtos {

    private ScheduleDtos() { }

    private static final String TYPE_PATTERN = "class|work|personal";
    private static final String TYPE_MESSAGE = "Type must be class, work or personal";
    private static final String COMMITMENT_TYPE_PATTERN = "class|work|personal|extracurricular|other";
    private static final String COMMITMENT_TYPE_MESSAGE = "Type must be class, work, personal, extracurricular or other";


    public record EventRequest(
            @NotBlank(message = "Please enter an event name.")
            @Size(max = 160, message = "Event name must be at most 160 characters") String title,
            @NotBlank @Pattern(regexp = TYPE_PATTERN, message = TYPE_MESSAGE) String eventType,
            @NotNull(message = "Date is required") LocalDate eventDate,
            @NotNull(message = "Start time is required") LocalTime startTime,
            @NotNull(message = "End time is required") LocalTime endTime,
            @Size(max = 180, message = "Location must be at most 180 characters") String location,
            @Size(max = 500, message = "Notes must be at most 500 characters") String notes) { }

    public record EventResponse(Long id, String title, String eventType, LocalDate eventDate,
                                LocalTime startTime, LocalTime endTime, String location, String notes) {
        static EventResponse from(ScheduleEvent e) {
            return new EventResponse(e.getId(), e.getTitle(), e.getEventType(), e.getEventDate(),
                    e.getStartTime(), e.getEndTime(), e.getLocation(), e.getNotes());
        }
    }

    public record CommitmentRequest(
            @NotBlank(message = "Please enter a name.")
            @Size(max = 160, message = "Name must be at most 160 characters") String title,
            @NotBlank @Pattern(regexp = COMMITMENT_TYPE_PATTERN, message = COMMITMENT_TYPE_MESSAGE) String commitmentType,
            @NotNull @Min(value = 1, message = "Day must be 1 (Monday) to 7 (Sunday)")
            @Max(value = 7, message = "Day must be 1 (Monday) to 7 (Sunday)") Integer dayOfWeek,
            @NotNull(message = "Start time is required") LocalTime startTime,
            @NotNull(message = "End time is required") LocalTime endTime,
            @NotNull(message = "Start date is required") LocalDate startDate,
            @NotNull(message = "End date is required") LocalDate endDate,
            @Pattern(regexp = "#[0-9A-Fa-f]{6}", message = "Color must look like #66b366") String color) { }

    public record CommitmentResponse(Long id, String title, String commitmentType, Integer dayOfWeek,
                                     LocalTime startTime, LocalTime endTime, LocalDate startDate,
                                     LocalDate endDate, String color) {
        static CommitmentResponse from(WeeklyCommitment c) {
            return new CommitmentResponse(c.getId(), c.getTitle(), c.getCommitmentType(), c.getDayOfWeek(),
                    c.getStartTime(), c.getEndTime(), c.getStartDate(), c.getEndDate(), c.getColor());
        }
    }


    public record TermRequest(
            @NotBlank(message = "Term name is required")
            @Size(max = 100, message = "Term name must be at most 100 characters") String name,
            @NotNull(message = "Start date is required") LocalDate startDate,
            @NotNull(message = "End date is required") LocalDate endDate) { }

    public record TermResponse(Long id, String name, LocalDate startDate, LocalDate endDate) {
        static TermResponse from(Term t) {
            return new TermResponse(t.getId(), t.getName(), t.getStartDate(), t.getEndDate());
        }
    }


    public record PreferencesRequest(
            @NotNull(message = "Choose an earliest class time") LocalTime earliestClassTime,
            @NotNull(message = "Choose a latest class time") LocalTime latestClassTime,
            @NotNull @Min(value = 3, message = "Maximum credits must be from 3 to 21")
            @Max(value = 21, message = "Maximum credits must be from 3 to 21") Integer maxCredits,
            @NotNull List<@Pattern(regexp = "MON|TUE|WED|THU|FRI|SAT|SUN",
                    message = "Days must be MON to SUN") String> preferredDays,
            @NotNull Boolean minimizeGaps,
            @NotNull Boolean minimizeCampusDays) { }

    public record PreferencesResponse(LocalTime earliestClassTime, LocalTime latestClassTime, Integer maxCredits,
                                      List<String> preferredDays, boolean minimizeGaps,
                                      boolean minimizeCampusDays) {
        static PreferencesResponse from(SchedulePreferences p) {
            return new PreferencesResponse(p.getEarliestClassTime(), p.getLatestClassTime(), p.getMaxCredits(),
                    p.getPreferredDayList(), p.isMinimizeGaps(), p.isMinimizeCampusDays());
        }
    }

    public record GeneratedRequest(
            @NotNull(message = "Choose a term first") Long termId,
            @NotBlank(message = "Enter a name for your schedule option")
            @Size(max = 120, message = "Name must be at most 120 characters") String scheduleName,
            @DecimalMin("0") @DecimalMax("100") BigDecimal score) { }

    public record GeneratedResponse(Long id, Long termId, String scheduleName, BigDecimal score,
                                    @JsonProperty("isSelected") boolean isSelected, Instant createdAt) {
        static GeneratedResponse from(GeneratedSchedule g) {
            return new GeneratedResponse(g.getId(), g.getTermId(), g.getScheduleName(), g.getScore(),
                    g.isSelected(), g.getCreatedAt());
        }
    }
}