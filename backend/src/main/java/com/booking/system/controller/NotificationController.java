package com.booking.system.controller;

import com.booking.system.entity.Notification;
import com.booking.system.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(notificationService.getNotifications(userId, unreadOnly));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestParam Long userId) {
        return ResponseEntity.ok(Map.of("count", notificationService.countUnread(userId)));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(@RequestParam Long userId) {
        return ResponseEntity.ok(Map.of("updated", notificationService.markAllAsRead(userId)));
    }
}
