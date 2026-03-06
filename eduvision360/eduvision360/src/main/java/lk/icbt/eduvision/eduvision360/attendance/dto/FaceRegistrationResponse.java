package lk.icbt.eduvision.eduvision360.attendance.dto;

import lombok.Data;

@Data
public class FaceRegistrationResponse {
    private boolean success;
    private String studentId;
    private Integer samplesSaved;
    private String message;
}