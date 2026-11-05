package com.campuspath.backend.notification;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campuspath.backend.schedule.ScheduleEvent;
import com.campuspath.backend.schedule.ScheduleEventRepository;
import com.campuspath.backend.schedule.WeeklyCommitment;
import com.campuspath.backend.schedule.WeeklyCommitmentRepository;
import com.campuspath.backend.user.User;
import com.campuspath.backend.user.UserRepository;
import com.campuspath.backend.user.UserSettings;
import com.campuspath.backend.user.UserSettingsRepository;

@Service
public class ReminderService {

    private final UserRepository users;
    private final UserSettingsRepository settings;
    private final ScheduleEventRepository events;
    private final WeeklyCommitmentRepository commitments;
    private final NotificationRepository notifications;

    public ReminderService(UserRepository users, UserSettingsRepository settings, ScheduleEventRepository events,
                           WeeklyCommitmentRepository commitments, NotificationRepository notifications) {
        this.users = users;
        this.settings = settings;
        this.events = events;
        this.commitments = commitments;
        this.notifications = notifications;
    }

    @Transactional
    public int generateFor(Integer userId, Instant now) {
        User user = users.findById(userId).orElse(null);
        if (user == null) return 0;

        UserSettings s = settings.findById(userId).orElse(null);
        if (s != null && !s.isNotificationsEnabled()) return 0;
        int minutes = s != null ? s.getDefaultReminderMinutes() : 15;
        ZoneId zone = zoneOf(s != null ? s.getTimezone() : user.getUniversity().getTimezone());

        LocalDate today = now.atZone(zone).toLocalDate();
        LocalDate last = today.plusDays(8);

        List<ReminderPlanner.Occurrence> upcoming = new ArrayList<>();
        for (ScheduleEvent e : events.findByUserIdAndEventDateBetweenOrderByEventDateAscStartTimeAsc(userId, today, last)) {
            upcoming.add(new ReminderPlanner.Occurrence("event", e.getId(), e.getTitle(), e.getEventDate(),
                    e.getStartTime(), e.getLocation()));
        }
        List<WeeklyCommitment> weekly = commitments.findByUserIdOrderByDayOfWeekAscStartTimeAsc(userId);
        for (LocalDate d = today; !d.isAfter(last); d = d.plusDays(1)) {
            for (WeeklyCommitment c : weekly) {
                if (c.occursOn(d)) {
                    upcoming.add(new ReminderPlanner.Occurrence("commitment", c.getId(), c.getTitle(), d,
                            c.getStartTime(), null));
                }
            }
        }

        int created = 0;
        for (ReminderPlanner.DueReminder r : ReminderPlanner.due(upcoming, zone, now, minutes)) {
            created += notifications.insertIfAbsent(userId, r.title(), r.message(), r.notifyAt(),
                    r.sourceType(), r.sourceKey());
        }
        return created;
    }

    private static ZoneId zoneOf(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException | NullPointerException e) {
            return ZoneId.of("America/Chicago");
        }
    }
}