package com.campuspath.backend.notification;

import java.time.Instant;

import jakarta.persistence.*;

@Entity
@Table(name = "notifications", schema = "public")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "notify_at", nullable = false)
    private Instant notifyAt;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType;

    @Column(name = "source_key", nullable = false, length = 100)
    private String sourceKey;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected Notification() { }

    public Notification(Integer userId, String title, String message, Instant notifyAt,
                        String sourceType, String sourceKey) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notifyAt = notifyAt;
        this.sourceType = sourceType;
        this.sourceKey = sourceKey;
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public Instant getNotifyAt() { return notifyAt; }
    public boolean isRead() { return read; }
    public String getSourceType() { return sourceType; }
    public String getSourceKey() { return sourceKey; }
    public Instant getCreatedAt() { return createdAt; }

    public void markRead() { this.read = true; }
}