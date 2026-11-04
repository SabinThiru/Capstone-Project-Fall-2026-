package com.campuspath.backend.schedule;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

@Entity
@Table(name = "schedule_preferences", schema = "public")
public class SchedulePreferences {

    public static final List<String> ALL_DAYS = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");

    private static final LocalTime DEFAULT_EARLIEST = LocalTime.of(8, 0);
    private static final LocalTime DEFAULT_LATEST = LocalTime.of(20, 0);
    private static final int DEFAULT_MAX_CREDITS = 15;
    private static final Short[] DEFAULT_DAYS = {1, 2, 3, 4, 5};

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preference_id")
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "earliest_class_time")
    private LocalTime earliestClassTime = DEFAULT_EARLIEST;

    @Column(name = "latest_class_time")
    private LocalTime latestClassTime = DEFAULT_LATEST;

    @Column(name = "max_credits")
    private Integer maxCredits = DEFAULT_MAX_CREDITS;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "preferred_days")
    private Short[] preferredDays = DEFAULT_DAYS.clone();

    @Column(name = "minimize_gaps")
    private Boolean minimizeGaps = false;

    @Column(name = "minimize_campus_days")
    private Boolean minimizeCampusDays = false;

    protected SchedulePreferences() { }

    public SchedulePreferences(Long userId) {
        this.userId = userId;
    }

    public static SchedulePreferences defaults(Long userId) {
        return new SchedulePreferences(userId);
    }

    public List<Integer> getPreferredDayNumbers() {
        if (preferredDays == null) return List.of();
        return Arrays.stream(preferredDays).filter(Objects::nonNull).map(Short::intValue)
                .filter(n -> n >= 1 && n <= 7).distinct().sorted().toList();
    }

    public List<String> getPreferredDayList() {
        return getPreferredDayNumbers().stream().map(n -> ALL_DAYS.get(n - 1)).toList();
    }

    public void update(LocalTime earliest, LocalTime latest, int maxCredits, List<String> days,
                       boolean minimizeGaps, boolean minimizeCampusDays) {
        this.earliestClassTime = earliest;
        this.latestClassTime = latest;
        this.maxCredits = maxCredits;

        this.preferredDays = ALL_DAYS.stream().filter(days::contains)
                .map(d -> (short) (ALL_DAYS.indexOf(d) + 1)).toArray(Short[]::new);
        this.minimizeGaps = minimizeGaps;
        this.minimizeCampusDays = minimizeCampusDays;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public LocalTime getEarliestClassTime() { return earliestClassTime != null ? earliestClassTime : DEFAULT_EARLIEST; }
    public LocalTime getLatestClassTime() { return latestClassTime != null ? latestClassTime : DEFAULT_LATEST; }
    public Integer getMaxCredits() { return maxCredits != null ? maxCredits : DEFAULT_MAX_CREDITS; }
    public boolean isMinimizeGaps() { return Boolean.TRUE.equals(minimizeGaps); }
    public boolean isMinimizeCampusDays() { return Boolean.TRUE.equals(minimizeCampusDays); }
}