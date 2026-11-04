package com.campuspath.backend.notification;

import java.time.Instant;
import java.util.List;

public final class NotificationDtos {

    private NotificationDtos() { }

    public record NotificationResponse(Integer id, String title, String message, Instant notifyAt,
                                       boolean read, String sourceType) {
        static NotificationResponse from(Notification n) {
            return new NotificationResponse(n.getId(), n.getTitle(), n.getMessage(), n.getNotifyAt(),
                    n.isRead(), n.getSourceType());
        }
    }

    public record NotificationListResponse(long unreadCount, List<NotificationResponse> items) { }
}