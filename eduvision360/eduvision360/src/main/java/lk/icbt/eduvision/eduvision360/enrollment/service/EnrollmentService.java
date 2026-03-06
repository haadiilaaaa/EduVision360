package lk.icbt.eduvision.eduvision360.enrollment.service;

import lk.icbt.eduvision.eduvision360.enrollment.dto.EnrollCourseRequest;
import lk.icbt.eduvision.eduvision360.enrollment.dto.EnrollmentResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface EnrollmentService {

    EnrollmentResponse enroll(EnrollCourseRequest req, Authentication authentication);

    List<EnrollmentResponse> myEnrollments(Authentication authentication);
}