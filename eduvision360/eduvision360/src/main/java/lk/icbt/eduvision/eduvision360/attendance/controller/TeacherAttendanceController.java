package lk.icbt.eduvision.eduvision360.attendance.controller;

import lk.icbt.eduvision.eduvision360.attendance.dto.TeacherAttendanceResponse;
import lk.icbt.eduvision.eduvision360.attendance.model.AttendanceStatus;
import lk.icbt.eduvision.eduvision360.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/teacher/attendance")
@RequiredArgsConstructor
public class TeacherAttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public List<TeacherAttendanceResponse> getAttendanceForTeacher(
            @RequestParam(required = false) String classId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        return attendanceService.getAttendanceForTeacher(
                authentication.getName(),
                classId,
                date,
                status,
                search
        );
    }
}