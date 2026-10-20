package com.campuspath.backend.user;

import jakarta.persistence.*;

@Entity
@Table(name = "user_settings", schema = "public")
public class UserSettings {

    @Id
    private Integer userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "America/Chicago";

    @Column(name = "theme", nullable = false, length = 20)
    private String theme = "system";

    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = true;

    @Column(name = "email_reminders_enabled", nullable = false)
    private boolean emailRemindersEnabled = true;

    @Column(name = "default_reminder_minutes", nullable = false)
    private int defaultReminderMinutes = 15;

    protected UserSettings() { }

    public UserSettings(User user, String timezone) {
        this.user = user;
        if (timezone != null) this.timezone = timezone;
    }

    public String getTimezone() { return timezone; }
    public String getTheme() { return theme; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public boolean isEmailRemindersEnabled() { return emailRemindersEnabled; }
    public int getDefaultReminderMinutes() { return defaultReminderMinutes; }

    public void setTimezone(String v) { this.timezone = v; }
    public void setTheme(String v) { this.theme = v; }
    public void setNotificationsEnabled(boolean v) { this.notificationsEnabled = v; }
    public void setEmailRemindersEnabled(boolean v) { this.emailRemindersEnabled = v; }
    public void setDefaultReminderMinutes(int v) { this.defaultReminderMinutes = v; }
}