package edu.campus.findit.controller;

import edu.campus.findit.dto.response.ApiResponse;
import edu.campus.findit.dto.response.NotificationResponse;
import edu.campus.findit.model.User;
import edu.campus.findit.service.AuthService;
import edu.campus.findit.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthService authService;

    public NotificationController(NotificationService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        List<NotificationResponse> list = notificationService.getNotificationsForUser(currentUser);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        long count = notificationService.getUnreadCount(currentUser);
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        notificationService.markAsRead(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        User currentUser = authService.getCurrentAuthenticatedUser();
        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }
}

