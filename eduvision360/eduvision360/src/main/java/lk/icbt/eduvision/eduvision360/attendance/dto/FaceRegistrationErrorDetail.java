package lk.icbt.eduvision.eduvision360.attendance.dto;

import lombok.Data;

import java.util.List;

@Data
public class FaceRegistrationErrorDetail {
    private String message;
    private String studentId;
    private Integer validSamples;
    private Integer requiredMinimum;
    private List<InvalidFaceFile> invalidFiles;
}