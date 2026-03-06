package lk.icbt.eduvision.eduvision360.course.service;

import lk.icbt.eduvision.eduvision360.auth.model.User;
import lk.icbt.eduvision.eduvision360.auth.model.UserRole;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.course.dto.CreateCourseRequest;
import lk.icbt.eduvision.eduvision360.course.dto.CourseResponse;
import lk.icbt.eduvision.eduvision360.course.dto.UpdateCourseRequest;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.department.model.Department;
import lk.icbt.eduvision.eduvision360.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    public CourseResponse create(CreateCourseRequest req) {
        String code = req.getCourseCode().trim().toUpperCase();

        if (courseRepository.existsByCourseCode(code)) {
            throw new IllegalArgumentException("Course code already exists");
        }

        Department department = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        validateTeacher(req.getTeacherId());

        Course course = Course.builder()
                .courseCode(code)
                .title(req.getTitle().trim())
                .description(req.getDescription() != null ? req.getDescription().trim() : null)
                .departmentId(department.getId())
                .teacherId(isBlank(req.getTeacherId()) ? null : req.getTeacherId())
                .creditValue(req.getCreditValue())
                .semester(req.getSemester())
                .academicYear(req.getAcademicYear().trim())
                .createdAt(Instant.now())
                .build();

        return toResponse(courseRepository.save(course));
    }

    @Override
    public List<CourseResponse> getAll() {
        return courseRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Course::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CourseResponse getById(String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        return toResponse(course);
    }

    @Override
    public CourseResponse update(String id, UpdateCourseRequest req) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        String code = req.getCourseCode().trim().toUpperCase();

        if (!course.getCourseCode().equals(code) && courseRepository.existsByCourseCode(code)) {
            throw new IllegalArgumentException("Course code already exists");
        }

        Department department = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        validateTeacher(req.getTeacherId());

        course.setCourseCode(code);
        course.setTitle(req.getTitle().trim());
        course.setDescription(req.getDescription() != null ? req.getDescription().trim() : null);
        course.setDepartmentId(department.getId());
        course.setTeacherId(isBlank(req.getTeacherId()) ? null : req.getTeacherId());
        course.setCreditValue(req.getCreditValue());
        course.setSemester(req.getSemester());
        course.setAcademicYear(req.getAcademicYear().trim());
        course.setStatus(req.getStatus());

        return toResponse(courseRepository.save(course));
    }

    @Override
    public void delete(String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        courseRepository.delete(course);
    }

    @Override
    public List<CourseResponse> getMyCourses(String teacherId) {
        return courseRepository.findByTeacherId(teacherId)
                .stream()
                .sorted(Comparator.comparing(Course::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    private void validateTeacher(String teacherId) {
        if (isBlank(teacherId)) return;

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Assigned teacher not found"));

        if (teacher.getRole() != UserRole.TEACHER) {
            throw new IllegalArgumentException("Assigned user is not a teacher");
        }
    }

    private CourseResponse toResponse(Course course) {
        String departmentName = departmentRepository.findById(course.getDepartmentId())
                .map(Department::getName)
                .orElse(null);

        String teacherName = null;
        if (!isBlank(course.getTeacherId())) {
            teacherName = userRepository.findById(course.getTeacherId())
                    .map(User::getFullName)
                    .orElse(null);
        }

        return new CourseResponse(
                course.getId(),
                course.getCourseCode(),
                course.getTitle(),
                course.getDescription(),
                course.getDepartmentId(),
                departmentName,
                course.getTeacherId(),
                teacherName,
                course.getCreditValue(),
                course.getSemester(),
                course.getAcademicYear(),
                course.getStatus(),
                course.getCreatedAt()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}