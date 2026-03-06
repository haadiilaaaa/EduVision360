package lk.icbt.eduvision.eduvision360.notification.controller;

import lk.icbt.eduvision.eduvision360.notification.dto.NotificationResponse;
import lk.icbt.eduvision.eduvision360.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ✅ Backward compatible: GET /my still works
    // ✅ New: /my?unreadOnly=true&limit=20
    @GetMapping("/my")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(notificationService.getMyNotifications(authentication, unreadOnly, limit));
    }

    @GetMapping("/my/unread")
    public ResponseEntity<List<NotificationResponse>> getMyUnreadNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(notificationService.getMyNotifications(authentication, true, limit));
    }

    @GetMapping("/my/unread-count")
    public ResponseEntity<Map<String, Long>> getMyUnreadCount(Authentication authentication) {
        long count = notificationService.getMyUnreadCount(authentication);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable String notificationId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId, authentication));
    }

    @PutMapping("/my/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.internalServerError()
                .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "Internal server error"));
    }
}