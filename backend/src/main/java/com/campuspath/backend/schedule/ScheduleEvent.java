package com.campuspath.backend.schedule;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;

@Entity
@Table(name = "calendar_events", schema = "public")
public class ScheduleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "event_type", nullable = false, length = 20)
    private String eventType;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "location", length = 180)
    private String location;

    @Column(name = "notes", length = 500)
    private String notes;

    protected ScheduleEvent() { }

    public ScheduleEvent(Integer userId) {
        this.userId = userId;
    }

    public void update(String title, String eventType, LocalDate eventDate, LocalTime startTime,
                       LocalTime endTime, String location, String notes) {
        this.title = title;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public Integer getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getEventType() { return eventType; }
    public LocalDate getEventDate() { return eventDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public String getNotes() { return notes; }
}