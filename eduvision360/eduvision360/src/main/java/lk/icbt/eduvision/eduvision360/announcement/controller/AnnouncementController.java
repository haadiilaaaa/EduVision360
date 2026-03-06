package lk.icbt.eduvision.eduvision360.announcement.controller;

import jakarta.validation.Valid;
import lk.icbt.eduvision.eduvision360.announcement.dto.AnnouncementResponse;
import lk.icbt.eduvision.eduvision360.announcement.dto.CreateAnnouncementRequest;
import lk.icbt.eduvision.eduvision360.announcement.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    // Teacher endpoints
    @PostMapping("/api/teacher/announcements")
    public ResponseEntity<AnnouncementResponse> create(
            @Valid @RequestBody CreateAnnouncementRequest req,
            Authentication authentication
    ) {
        return ResponseEntity.status(201)
                .body(announcementService.create(req, authentication.getName()));
    }

    @PutMapping("/api/teacher/announcements/{announcementId}")
    public ResponseEntity<AnnouncementResponse> update(
            @PathVariable String announcementId,
            @Valid @RequestBody CreateAnnouncementRequest req,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                announcementService.updateTeacherAnnouncement(authentication.getName(), announcementId, req)
        );
    }

    @GetMapping("/api/teacher/announcements/my")
    public ResponseEntity<List<AnnouncementResponse>> getMyAnnouncements(Authentication authentication) {
        return ResponseEntity.ok(
                announcementService.getTeacherAnnouncements(authentication.getName())
        );
    }

    @GetMapping("/api/teacher/announcements/course/{courseId}")
    public ResponseEntity<List<AnnouncementResponse>> getTeacherAnnouncementsByCourse(
            @PathVariable String courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                announcementService.getTeacherAnnouncementsByCourse(authentication.getName(), courseId)
        );
    }

    @DeleteMapping("/api/teacher/announcements/{announcementId}")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable String announcementId,
            Authentication authentication
    ) {
        announcementService.deleteTeacherAnnouncement(authentication.getName(), announcementId);
        return ResponseEntity.ok(Map.of("message", "Announcement deleted successfully"));
    }

    // Student endpoints
    @GetMapping("/api/student/announcements/course/{courseId}")
    public ResponseEntity<List<AnnouncementResponse>> getStudentAnnouncementsByCourse(
            @PathVariable String courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                announcementService.getStudentAnnouncementsForCourse(authentication.getName(), courseId)
        );
    }

    @GetMapping("/api/student/announcements/my-courses")
    public ResponseEntity<List<AnnouncementResponse>> getStudentAnnouncements(Authentication authentication) {
        return ResponseEntity.ok(
                announcementService.getStudentAnnouncementsForMyCourses(authentication.getName())
        );
    }
}