package com.campuspath.backend.notification;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.campuspath.backend.notification.NotificationDtos.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notifications;

    public NotificationController(NotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public NotificationListResponse list(@AuthenticationPrincipal Jwt jwt) {
        return notifications.list(jwt.getSubject());
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        return notifications.markRead(jwt.getSubject(), id);
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal Jwt jwt) {
        notifications.markAllRead(jwt.getSubject());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        notifications.delete(jwt.getSubject(), id);
    }
}