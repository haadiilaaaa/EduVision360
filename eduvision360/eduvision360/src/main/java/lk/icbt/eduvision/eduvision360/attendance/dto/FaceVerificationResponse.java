package lk.icbt.eduvision.eduvision360.attendance.dto;

import lombok.Data;

@Data
public class FaceVerificationResponse {

    private boolean recognized;
    private String studentId;   // must match the registered AI identity
    private Double confidence;
    private String message;
    private String status;      // MATCH / UNCERTAIN / NO_MATCH
}