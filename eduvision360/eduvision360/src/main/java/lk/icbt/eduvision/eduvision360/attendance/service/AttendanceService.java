package lk.icbt.eduvision.eduvision360.attendance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.icbt.eduvision.eduvision360.attendance.dto.AdminAttendanceResponse;
import lk.icbt.eduvision.eduvision360.attendance.dto.AttendanceMarkResponse;
import lk.icbt.eduvision.eduvision360.attendance.dto.FaceRegistrationErrorResponse;
import lk.icbt.eduvision.eduvision360.attendance.dto.FaceRegistrationResponse;
import lk.icbt.eduvision.eduvision360.attendance.dto.FaceVerificationResponse;
import lk.icbt.eduvision.eduvision360.attendance.dto.InvalidFaceFile;
import lk.icbt.eduvision.eduvision360.attendance.dto.TeacherAttendanceResponse;
import lk.icbt.eduvision.eduvision360.attendance.model.Attendance;
import lk.icbt.eduvision.eduvision360.attendance.model.AttendanceStatus;
import lk.icbt.eduvision.eduvision360.attendance.repository.AttendanceRepository;
import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.classsession.model.ClassSession;
import lk.icbt.eduvision.eduvision360.classsession.model.ClassSessionStatus;
import lk.icbt.eduvision.eduvision360.classsession.repository.ClassSessionRepository;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final RestTemplate restTemplate;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String AI_BASE_URL = "http://localhost:8000";

    // =====================================================
    // 1. FACE REGISTRATION
    // =====================================================
    public void registerFaceWithAI(MultipartFile[] images, Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("User is not authenticated");
        }

        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("Please provide face images");
        }

        String userId = authentication.getName();

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("student_id", currentUser.getEmail());

        try {
            for (MultipartFile image : images) {
                if (image == null || image.isEmpty()) {
                    continue;
                }

                ByteArrayResource resource = new ByteArrayResource(image.getBytes()) {
                    @Override
                    public String getFilename() {
                        return image.getOriginalFilename() != null
                                ? image.getOriginalFilename()
                                : "face.jpg";
                    }
                };

                body.add("files", resource);
            }

            if (!body.containsKey("files")) {
                throw new IllegalArgumentException("No valid face image provided");
            }

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<FaceRegistrationResponse> response =
                    restTemplate.postForEntity(
                            AI_BASE_URL + "/register-face",
                            request,
                            FaceRegistrationResponse.class
                    );

            FaceRegistrationResponse ai = response.getBody();

            if (!response.getStatusCode().is2xxSuccessful() || ai == null || !ai.isSuccess()) {
                throw new IllegalStateException(
                        ai != null && ai.getMessage() != null
                                ? ai.getMessage()
                                : "Face registration failed"
                );
            }

        } catch (RestClientResponseException e) {
            String cleanMessage = "Face registration failed. Please upload at least 3 clear face images with one visible face per image.";

            try {
                String rawBody = e.getResponseBodyAsString();

                if (rawBody != null && !rawBody.isBlank()) {
                    FaceRegistrationErrorResponse error =
                            objectMapper.readValue(rawBody, FaceRegistrationErrorResponse.class);

                    cleanMessage = buildFaceRegistrationErrorMessage(error);
                }
            } catch (Exception ignored) {
                // fallback message will be used
            }

            throw new IllegalStateException(cleanMessage, e);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read face image", e);

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(
                    e.getMessage() != null ? e.getMessage() : "Face registration failed",
                    e
            );
        }
    }

    // =====================================================
    // 2. VERIFY FACE ONLY
    // =====================================================
    public Object verifyFaceWithAI(MultipartFile image) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            ByteArrayResource resource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename() != null
                            ? image.getOriginalFilename()
                            : "verify.jpg";
                }
            };

            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Object> response = restTemplate.postForEntity(
                    AI_BASE_URL + "/verify-face",
                    request,
                    Object.class
            );

            return response.getBody();

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read verification image", e);
        }
    }

    // =====================================================
    // 3. MARK ATTENDANCE (SESSION-BASED)
    // =====================================================
    public AttendanceMarkResponse markAttendance(
            MultipartFile image,
            String sessionId,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("User is not authenticated");
        }

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Attendance image is required");
        }

        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID is required");
        }

        String userId = authentication.getName();

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        ClassSession session = classSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Class session not found"));

        if (session.getStatus() != ClassSessionStatus.OPEN) {
            throw new IllegalStateException("Attendance can only be marked for an OPEN class session");
        }

        if (session.getSessionDate() == null || !session.getSessionDate().equals(LocalDate.now())) {
            throw new IllegalStateException("Attendance can only be marked on the actual session date");
        }

        if (session.getCourseId() == null || session.getCourseId().isBlank()) {
            throw new IllegalStateException("This session is missing course information");
        }

        Course course = courseRepository.findById(session.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found for this class session"));

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(
                currentUser.getId(),
                course.getId()
        );

        if (!enrolled) {
            throw new IllegalStateException("You are not enrolled in this course");
        }

        if (attendanceRepository.existsByStudentIdAndSessionId(currentUser.getId(), session.getId())) {
            throw new IllegalStateException("Attendance already marked for this session");
        }

        String normalizedCourseCode = normalizeCourseCode(course.getCourseCode(), session.getCourseCode());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            ByteArrayResource resource = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename() != null
                            ? image.getOriginalFilename()
                            : "attendance.jpg";
                }
            };

            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<FaceVerificationResponse> response =
                    restTemplate.postForEntity(
                            AI_BASE_URL + "/verify-face",
                            request,
                            FaceVerificationResponse.class
                    );

            FaceVerificationResponse ai = response.getBody();

            if (ai == null) {
                return AttendanceMarkResponse.builder()
                        .success(false)
                        .status("REJECTED")
                        .message("AI service returned an empty response")
                        .confidence(0.0)
                        .classId(normalizedCourseCode)
                        .date(session.getSessionDate().toString())
                        .time(LocalTime.now().toString())
                        .build();
            }

            if (ai.isRecognized()
                    && ai.getStudentId() != null
                    && !currentUser.getEmail().equalsIgnoreCase(ai.getStudentId())) {

                return AttendanceMarkResponse.builder()
                        .success(false)
                        .status("REJECTED")
                        .message("Uploaded face does not match the logged-in account")
                        .confidence(ai.getConfidence())
                        .classId(normalizedCourseCode)
                        .date(session.getSessionDate().toString())
                        .time(LocalTime.now().toString())
                        .build();
            }

            String aiStatus = ai.getStatus();

            if ("MATCH".equals(aiStatus) && ai.isRecognized()) {
                Attendance attendance = Attendance.builder()
                        .studentId(currentUser.getId())
                        .studentEmail(currentUser.getEmail())
                        .sessionId(session.getId())
                        .courseId(course.getId())
                        .courseCode(normalizedCourseCode)
                        .classId(normalizedCourseCode)
                        .date(session.getSessionDate())
                        .time(LocalTime.now())
                        .status(AttendanceStatus.PRESENT)
                        .confidence(ai.getConfidence())
                        .createdAt(LocalDateTime.now())
                        .build();

                Attendance saved = attendanceRepository.save(attendance);

                return AttendanceMarkResponse.builder()
                        .success(true)
                        .status("PRESENT")
                        .message("Attendance marked successfully")
                        .confidence(saved.getConfidence())
                        .classId(saved.getClassId())
                        .date(saved.getDate().toString())
                        .time(saved.getTime().toString())
                        .build();
            }

            if ("UNCERTAIN".equals(aiStatus)) {
                return AttendanceMarkResponse.builder()
                        .success(false)
                        .status("UNCERTAIN")
                        .message(ai.getMessage() != null ? ai.getMessage() : "Low confidence. Please retry.")
                        .confidence(ai.getConfidence())
                        .classId(normalizedCourseCode)
                        .date(session.getSessionDate().toString())
                        .time(LocalTime.now().toString())
                        .build();
            }

            return AttendanceMarkResponse.builder()
                    .success(false)
                    .status("REJECTED")
                    .message(ai.getMessage() != null ? ai.getMessage() : "Face not recognized")
                    .confidence(ai.getConfidence())
                    .classId(normalizedCourseCode)
                    .date(session.getSessionDate().toString())
                    .time(LocalTime.now().toString())
                    .build();

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read attendance image", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Attendance marking failed: " + e.getMessage(), e);
        }
    }

    public List<Attendance> getMyAttendance(Authentication authentication) {
        String userId = authentication.getName();

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        return attendanceRepository.findByStudentIdOrderByDateDescTimeDesc(currentUser.getId());
    }

    // =====================================================
    // 4. ADMIN VIEW ATTENDANCE
    // =====================================================
    public List<AdminAttendanceResponse> getAttendanceForAdmin(
            String classId,
            LocalDate date,
            AttendanceStatus status,
            String search
    ) {
        String normalizedClassId = normalizeFilter(classId);
        String normalizedSearch = normalizeSearch(search);

        return attendanceRepository.findAll()
                .stream()
                .filter(a -> normalizedClassId.isBlank() || matchesCourseFilter(a, normalizedClassId))
                .filter(a -> date == null || (a.getDate() != null && a.getDate().equals(date)))
                .filter(a -> status == null || a.getStatus() == status)
                .sorted(Comparator.comparing(
                        Attendance::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(a -> {
                    User user = userRepository.findById(a.getStudentId()).orElse(null);

                    String studentName = user != null && user.getFullName() != null
                            ? user.getFullName()
                            : a.getStudentEmail();

                    String studentEmail = user != null && user.getEmail() != null
                            ? user.getEmail()
                            : a.getStudentEmail();

                    String displayClassId = resolveDisplayClassId(a);

                    return new AdminAttendanceResponse(
                            a.getId(),
                            studentName,
                            studentEmail,
                            displayClassId,
                            a.getDate() != null ? a.getDate().toString() : "",
                            a.getTime() != null ? a.getTime().toString() : "",
                            a.getStatus() != null ? a.getStatus().name() : "",
                            a.getConfidence()
                    );
                })
                .filter(r ->
                        normalizedSearch.isBlank()
                                || contains(r.studentName(), normalizedSearch)
                                || contains(r.studentEmail(), normalizedSearch)
                                || contains(r.classId(), normalizedSearch)
                )
                .toList();
    }

    // =====================================================
    // 5. TEACHER VIEW ATTENDANCE
    // =====================================================
    public List<TeacherAttendanceResponse> getAttendanceForTeacher(
            String teacherId,
            String classId,
            LocalDate date,
            AttendanceStatus status,
            String search
    ) {
        List<Course> myCourses = courseRepository.findByTeacherId(teacherId);

        Set<String> allowedCourseIds = new HashSet<>();
        Set<String> allowedCourseCodes = new HashSet<>();

        for (Course c : myCourses) {
            if (c.getId() != null && !c.getId().isBlank()) {
                allowedCourseIds.add(c.getId());
            }
            if (c.getCourseCode() != null && !c.getCourseCode().isBlank()) {
                allowedCourseCodes.add(c.getCourseCode().trim().toUpperCase());
            }
        }

        if (allowedCourseIds.isEmpty() && allowedCourseCodes.isEmpty()) {
            return List.of();
        }

        String normalizedClassId = normalizeFilter(classId);
        String normalizedSearch = normalizeSearch(search);

        return attendanceRepository.findAll()
                .stream()
                .filter(a -> belongsToTeacherCourses(a, allowedCourseIds, allowedCourseCodes))
                .filter(a -> normalizedClassId.isBlank() || matchesCourseFilter(a, normalizedClassId))
                .filter(a -> date == null || (a.getDate() != null && a.getDate().equals(date)))
                .filter(a -> status == null || a.getStatus() == status)
                .sorted(Comparator.comparing(
                        Attendance::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(a -> {
                    User user = userRepository.findById(a.getStudentId()).orElse(null);

                    String studentName = user != null && user.getFullName() != null
                            ? user.getFullName()
                            : a.getStudentEmail();

                    String studentEmail = user != null && user.getEmail() != null
                            ? user.getEmail()
                            : a.getStudentEmail();

                    String displayClassId = resolveDisplayClassId(a);

                    return new TeacherAttendanceResponse(
                            a.getId(),
                            studentName,
                            studentEmail,
                            displayClassId,
                            a.getDate() != null ? a.getDate().toString() : "",
                            a.getTime() != null ? a.getTime().toString() : "",
                            a.getStatus() != null ? a.getStatus().name() : "",
                            a.getConfidence()
                    );
                })
                .filter(r ->
                        normalizedSearch.isBlank()
                                || contains(r.studentName(), normalizedSearch)
                                || contains(r.studentEmail(), normalizedSearch)
                                || contains(r.classId(), normalizedSearch)
                )
                .toList();
    }

    // =====================================================
    // HELPERS
    // =====================================================
    private String buildFaceRegistrationErrorMessage(FaceRegistrationErrorResponse error) {
        if (error == null || error.getDetail() == null) {
            return "Face registration failed. Please upload at least 3 clear face images with one visible face per image.";
        }

        var detail = error.getDetail();

        StringBuilder message = new StringBuilder();

        if (detail.getMessage() != null && !detail.getMessage().isBlank()) {
            message.append(detail.getMessage());
        } else {
            message.append("Face registration failed.");
        }

        if (detail.getValidSamples() != null && detail.getRequiredMinimum() != null) {
            message.append(" Only ")
                    .append(detail.getValidSamples())
                    .append(" valid sample(s) were accepted out of required ")
                    .append(detail.getRequiredMinimum())
                    .append(".");
        }

        if (detail.getInvalidFiles() != null && !detail.getInvalidFiles().isEmpty()) {
            message.append(" Invalid files: ");

            for (int i = 0; i < detail.getInvalidFiles().size(); i++) {
                InvalidFaceFile file = detail.getInvalidFiles().get(i);

                message.append(file.getFile() != null ? file.getFile() : "unknown-file")
                        .append(" (")
                        .append(file.getReason() != null ? file.getReason() : "invalid image")
                        .append(")");

                if (i < detail.getInvalidFiles().size() - 1) {
                    message.append(", ");
                }
            }
        }

        return message.toString();
    }

    private String normalizeCourseCode(String courseCode, String fallbackCourseCode) {
        if (courseCode != null && !courseCode.isBlank()) {
            return courseCode.trim().toUpperCase();
        }
        if (fallbackCourseCode != null && !fallbackCourseCode.isBlank()) {
            return fallbackCourseCode.trim().toUpperCase();
        }
        return "";
    }

    private String normalizeFilter(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String normalizeSearch(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }

    private boolean matchesCourseFilter(Attendance attendance, String filter) {
        return equalsIgnoreCase(attendance.getCourseId(), filter)
                || equalsIgnoreCase(attendance.getCourseCode(), filter)
                || equalsIgnoreCase(attendance.getClassId(), filter);
    }

    private boolean belongsToTeacherCourses(
            Attendance attendance,
            Set<String> allowedCourseIds,
            Set<String> allowedCourseCodes
    ) {
        boolean matchesCourseId = attendance.getCourseId() != null
                && allowedCourseIds.contains(attendance.getCourseId());

        boolean matchesCourseCode = attendance.getCourseCode() != null
                && allowedCourseCodes.contains(attendance.getCourseCode().trim().toUpperCase());

        boolean matchesLegacyClassId = attendance.getClassId() != null
                && allowedCourseCodes.contains(attendance.getClassId().trim().toUpperCase());

        return matchesCourseId || matchesCourseCode || matchesLegacyClassId;
    }

    private boolean equalsIgnoreCase(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }

    private String resolveDisplayClassId(Attendance attendance) {
        if (attendance.getCourseCode() != null && !attendance.getCourseCode().isBlank()) {
            return attendance.getCourseCode();
        }
        if (attendance.getClassId() != null && !attendance.getClassId().isBlank()) {
            return attendance.getClassId();
        }
        if (attendance.getCourseId() != null) {
            return attendance.getCourseId();
        }
        return "";
    }
}