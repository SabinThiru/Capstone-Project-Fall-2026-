package com.campuspath.backend.notification;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.campuspath.backend.user.User;
import com.campuspath.backend.user.UserRepository;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "app.reminders.scheduler-enabled", havingValue = "true", matchIfMissing = true)
public class ReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

    private final UserRepository users;
    private final ReminderService reminders;

    public ReminderScheduler(UserRepository users, ReminderService reminders) {
        this.users = users;
        this.reminders = reminders;
    }

    @Scheduled(initialDelay = 30_000, fixedDelay = 60_000)
    public void run() {
        Instant now = Instant.now();
        for (User user : users.findAll()) {
            try {
                reminders.generateFor(user.getId(), now);
            } catch (RuntimeException e) {
                log.warn("Could not create reminders for user {}", user.getId(), e);
            }
        }
    }
}