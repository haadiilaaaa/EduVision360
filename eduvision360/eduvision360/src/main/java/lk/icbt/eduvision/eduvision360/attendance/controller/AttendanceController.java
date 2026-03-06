package lk.icbt.eduvision.eduvision360.attendance.controller;

import lk.icbt.eduvision.eduvision360.attendance.dto.AttendanceMarkResponse;
import lk.icbt.eduvision.eduvision360.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/register-face")
    public ResponseEntity<?> registerFace(
            @RequestParam("images") MultipartFile[] images,
            Authentication authentication
    ) {
        attendanceService.registerFaceWithAI(images, authentication);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Face registered successfully",
                "samples", images.length
        ));
    }

    // ✅ UPDATED: MARK ATTENDANCE USING sessionId
    @PostMapping("/mark")
    public ResponseEntity<AttendanceMarkResponse> markAttendance(
            @RequestParam("image") MultipartFile image,
            @RequestParam("sessionId") String sessionId,
            Authentication authentication
    ) {
        AttendanceMarkResponse result = attendanceService.markAttendance(image, sessionId, authentication);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my")
    public ResponseEntity<?> myAttendance(Authentication authentication) {
        return ResponseEntity.ok(attendanceService.getMyAttendance(authentication));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<?> handleDuplicateKey(DuplicateKeyException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Attendance already marked for this session"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", ex.getMessage() != null ? ex.getMessage() : "Internal server error"));
    }
}