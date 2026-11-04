package com.campuspath.backend.schedule;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;

@Entity
@Table(name = "commitments", schema = "public")
public class WeeklyCommitment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commitment_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "commitment_type", nullable = false, length = 20)
    private String commitmentType;

    @Column(name = "day_of_week")
    private Short dayOfWeek;   // 1 = Monday ... 7 = Sunday (SMALLINT)

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;   // null = no end date

    @Column(name = "color", length = 7)
    private String color;

    protected WeeklyCommitment() { }

    public WeeklyCommitment(Long userId) {
        this.userId = userId;
    }

    public void update(String title, String commitmentType, Integer dayOfWeek, LocalTime startTime,
                       LocalTime endTime, LocalDate startDate, LocalDate endDate, String color) {
        this.title = title;
        this.commitmentType = commitmentType;
        this.dayOfWeek = dayOfWeek == null ? null : dayOfWeek.shortValue();
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.color = color;
    }


    public boolean occursOn(LocalDate date) {
        return dayOfWeek != null
                && date.getDayOfWeek().getValue() == dayOfWeek
                && !date.isBefore(startDate)
                && (endDate == null || !date.isAfter(endDate));
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getCommitmentType() { return commitmentType; }
    public Integer getDayOfWeek() { return dayOfWeek == null ? null : dayOfWeek.intValue(); }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getColor() { return color; }
}