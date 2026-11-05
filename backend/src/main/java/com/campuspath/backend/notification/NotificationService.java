package com.campuspath.backend.notification;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.campuspath.backend.common.ApiException;
import com.campuspath.backend.notification.NotificationDtos.*;
import com.campuspath.backend.user.CurrentUserService;

@Service
public class NotificationService {

    private final CurrentUserService currentUser;
    private final NotificationRepository notifications;
    private final ReminderService reminders;

    public NotificationService(CurrentUserService currentUser, NotificationRepository notifications,
                               ReminderService reminders) {
        this.currentUser = currentUser;
        this.notifications = notifications;
        this.reminders = reminders;
    }
    
    @Transactional
    public NotificationListResponse list(String sub) {
        Integer userId = currentUser.requireId(sub);
        Instant now = Instant.now();
        reminders.generateFor(userId, now);
        return new NotificationListResponse(
                notifications.countByUserIdAndReadFalseAndNotifyAtLessThanEqual(userId, now),
                notifications.findTop50ByUserIdAndNotifyAtLessThanEqualOrderByNotifyAtDesc(userId, now)
                        .stream().map(NotificationResponse::from).toList());
    }

    @Transactional
    public NotificationResponse markRead(String sub, Integer id) {
        Notification n = find(sub, id);
        n.markRead();
        return NotificationResponse.from(n);
    }

    @Transactional
    public void markAllRead(String sub) {
        notifications.markAllRead(currentUser.requireId(sub));
    }

    @Transactional
    public void delete(String sub, Integer id) {
        notifications.delete(find(sub, id));
    }

    private Notification find(String sub, Integer id) {
        return notifications.findByIdAndUserId(id, currentUser.requireId(sub))
                .orElseThrow(() -> ApiException.notFound("Notification not found"));
    }
}