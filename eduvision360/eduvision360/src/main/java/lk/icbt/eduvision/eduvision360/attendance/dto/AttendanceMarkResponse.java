package lk.icbt.eduvision.eduvision360.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceMarkResponse {

    private boolean success;
    private String status;      // PRESENT / UNCERTAIN / REJECTED
    private String message;
    private Double confidence;

    private String classId;
    private String date;
    private String time;
}