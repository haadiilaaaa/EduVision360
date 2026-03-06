package lk.icbt.eduvision.eduvision360.attendance.dto;

public record TeacherAttendanceResponse(
        String id,
        String studentName,
        String studentEmail,
        String classId,
        String date,
        String time,
        String status,
        Double confidence
) {}