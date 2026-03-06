package lk.icbt.eduvision.eduvision360.attendance.controller;

import lk.icbt.eduvision.eduvision360.attendance.dto.AdminAttendanceResponse;
import lk.icbt.eduvision.eduvision360.attendance.model.AttendanceStatus;
import lk.icbt.eduvision.eduvision360.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/attendance")
@RequiredArgsConstructor
public class AdminAttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public List<AdminAttendanceResponse> getAttendanceForAdmin(
            @RequestParam(required = false) String classId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(required = false) String search
    ) {
        return attendanceService.getAttendanceForAdmin(classId, date, status, search);
    }
}