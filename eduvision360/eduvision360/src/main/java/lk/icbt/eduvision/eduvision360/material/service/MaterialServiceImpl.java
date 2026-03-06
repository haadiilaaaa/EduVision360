package lk.icbt.eduvision.eduvision360.material.service;

import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.enrollment.model.Enrollment;
import lk.icbt.eduvision.eduvision360.enrollment.repository.EnrollmentRepository;
import lk.icbt.eduvision.eduvision360.material.dto.CreateMaterialRequest;
import lk.icbt.eduvision.eduvision360.material.dto.MaterialResponse;
import lk.icbt.eduvision.eduvision360.material.model.LearningMaterial;
import lk.icbt.eduvision.eduvision360.material.model.MaterialViewLog;
import lk.icbt.eduvision.eduvision360.material.repository.LearningMaterialRepository;
import lk.icbt.eduvision.eduvision360.material.repository.MaterialViewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final LearningMaterialRepository learningMaterialRepository;
    private final MaterialViewLogRepository materialViewLogRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Override
    public MaterialResponse create(CreateMaterialRequest req, String teacherId) {
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getTeacherId() == null || !course.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("You can only upload materials for your assigned courses");
        }

        LearningMaterial material = LearningMaterial.builder()
                .courseId(course.getId())
                .teacherId(teacherId)
                .title(req.getTitle().trim())
                .description(req.getDescription() != null ? req.getDescription().trim() : null)
                .type(req.getType())
                .content(req.getContent().trim())
                .createdAt(Instant.now())
                .build();

        return toResponse(learningMaterialRepository.save(material));
    }

    @Override
    public MaterialResponse updateTeacherMaterial(String teacherId, String materialId, CreateMaterialRequest req) {
        LearningMaterial material = learningMaterialRepository.findByIdAndTeacherId(materialId, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found or access denied"));

        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getTeacherId() == null || !course.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("You can only update materials for your assigned courses");
        }

        material.setCourseId(course.getId());
        material.setTitle(req.getTitle().trim());
        material.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
        material.setType(req.getType());
        material.setContent(req.getContent().trim());

        return toResponse(learningMaterialRepository.save(material));
    }

    @Override
    public List<MaterialResponse> getTeacherMaterials(String teacherId) {
        return learningMaterialRepository.findByTeacherIdOrderByCreatedAtDesc(teacherId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<MaterialResponse> getTeacherMaterialsByCourse(String teacherId, String courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (course.getTeacherId() == null || !course.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("You do not have access to this course");
        }

        return learningMaterialRepository.findByTeacherIdAndCourseIdOrderByCreatedAtDesc(teacherId, courseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void deleteTeacherMaterial(String teacherId, String materialId) {
        LearningMaterial material = learningMaterialRepository.findByIdAndTeacherId(materialId, teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found or access denied"));

        learningMaterialRepository.delete(material);
    }

    @Override
    public List<MaterialResponse> getStudentMaterialsForCourse(String studentId, String courseId) {
        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
        if (!enrolled) {
            throw new IllegalArgumentException("You are not enrolled in this course");
        }

        return learningMaterialRepository.findByCourseIdOrderByCreatedAtDesc(courseId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<MaterialResponse> getStudentMaterialsForMyCourses(String studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdOrderByEnrolledAtDesc(studentId);

        List<String> courseIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .distinct()
                .toList();

        if (courseIds.isEmpty()) {
            return List.of();
        }

        return learningMaterialRepository.findByCourseIdInOrderByCreatedAtDesc(courseIds)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public void trackStudentMaterialView(String studentId, String materialId) {
        LearningMaterial material = learningMaterialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found"));

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseId(studentId, material.getCourseId());
        if (!enrolled) {
            throw new IllegalArgumentException("You are not allowed to access this material");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Course course = courseRepository.findById(material.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        MaterialViewLog viewLog = MaterialViewLog.builder()
                .studentId(student.getId())
                .studentName(student.getFullName())
                .studentEmail(student.getEmail())
                .materialId(material.getId())
                .courseId(course.getId())
                .courseCode(course.getCourseCode())
                .courseTitle(course.getTitle())
                .teacherId(material.getTeacherId())
                .materialTitle(material.getTitle())
                .materialType(material.getType())
                .viewedAt(Instant.now())
                .build();

        materialViewLogRepository.save(viewLog);
    }

    private MaterialResponse toResponse(LearningMaterial material) {
        Course course = courseRepository.findById(material.getCourseId()).orElse(null);

        String courseCode = course != null ? course.getCourseCode() : null;
        String courseTitle = course != null ? course.getTitle() : null;

        String teacherName = userRepository.findById(material.getTeacherId())
                .map(User::getFullName)
                .orElse(null);

        return new MaterialResponse(
                material.getId(),
                material.getCourseId(),
                courseCode,
                courseTitle,
                material.getTeacherId(),
                teacherName,
                material.getTitle(),
                material.getDescription(),
                material.getType(),
                material.getContent(),
                material.getCreatedAt()
        );
    }
}