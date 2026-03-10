package lk.icbt.eduvision.eduvision360.admin.dashboard.service;

import lk.icbt.eduvision.eduvision360.admin.dashboard.dto.AdminChartsDto;
import lk.icbt.eduvision.eduvision360.admin.dashboard.dto.AdminDashboardSummaryResponse;
import lk.icbt.eduvision.eduvision360.admin.dashboard.dto.AdminOverviewDto;
import lk.icbt.eduvision.eduvision360.admin.dashboard.dto.ChartItemDto;
import lk.icbt.eduvision.eduvision360.admin.dashboard.dto.RecentActivityDto;
import lk.icbt.eduvision.eduvision360.announcement.model.CourseAnnouncement;
import lk.icbt.eduvision.eduvision360.announcement.repository.AnnouncementRepository;
import lk.icbt.eduvision.eduvision360.auth.model.UserRole;
import lk.icbt.eduvision.eduvision360.auth.model.UserStatus;
import lk.icbt.eduvision.eduvision360.auth.repository.UserRepository;
import lk.icbt.eduvision.eduvision360.classsession.model.ClassSession;
import lk.icbt.eduvision.eduvision360.classsession.model.ClassSessionStatus;
import lk.icbt.eduvision.eduvision360.classsession.repository.ClassSessionRepository;
import lk.icbt.eduvision.eduvision360.communication.model.DirectMessage;
import lk.icbt.eduvision.eduvision360.communication.repository.DirectMessageRepository;
import lk.icbt.eduvision.eduvision360.course.model.Course;
import lk.icbt.eduvision.eduvision360.course.repository.CourseRepository;
import lk.icbt.eduvision.eduvision360.department.model.Department;
import lk.icbt.eduvision.eduvision360.department.repository.DepartmentRepository;
import lk.icbt.eduvision.eduvision360.material.model.LearningMaterial;
import lk.icbt.eduvision.eduvision360.material.repository.LearningMaterialRepository;
import lk.icbt.eduvision.eduvision360.prediction.model.DropoutPrediction;
import lk.icbt.eduvision.eduvision360.prediction.repository.DropoutPredictionRepository;
import lk.icbt.eduvision.eduvision360.quiz.model.TeacherQuiz;
import lk.icbt.eduvision.eduvision360.quiz.repository.TeacherQuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final ClassSessionRepository classSessionRepository;
    private final TeacherQuizRepository teacherQuizRepository;
    private final AnnouncementRepository announcementRepository;
    private final LearningMaterialRepository learningMaterialRepository;
    private final DirectMessageRepository directMessageRepository;
    private final DropoutPredictionRepository dropoutPredictionRepository;

    @Override
    public AdminDashboardSummaryResponse getSummary() {
        AdminOverviewDto overview = buildOverview();
        AdminChartsDto charts = buildCharts();
        List<RecentActivityDto> recentActivities = buildRecentActivities();

        return AdminDashboardSummaryResponse.builder()
                .overview(overview)
                .charts(charts)
                .recentActivities(recentActivities)
                .build();
    }

    private AdminOverviewDto buildOverview() {
        long totalStudents = userRepository.countByRole(UserRole.STUDENT);
        long totalTeachers = userRepository.countByRole(UserRole.TEACHER);
        long totalAdmins = userRepository.countByRole(UserRole.ADMIN);
        long pendingTeachers = userRepository.countByRoleAndStatus(UserRole.TEACHER, UserStatus.PENDING_APPROVAL);

        long totalDepartments = departmentRepository.count();
        long totalCourses = courseRepository.count();
        long totalSessions = classSessionRepository.count();

        long totalQuizzes = teacherQuizRepository.count();
        long totalAnnouncements = announcementRepository.count();
        long totalMaterials = learningMaterialRepository.count();
        long totalMessages = directMessageRepository.count();

        long highRisk = dropoutPredictionRepository.countByRiskLevel("HIGH");
        long mediumRisk = dropoutPredictionRepository.countByRiskLevel("MEDIUM");
        long lowRisk = dropoutPredictionRepository.countByRiskLevel("LOW");

        return AdminOverviewDto.builder()
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .totalAdmins(totalAdmins)
                .pendingTeachers(pendingTeachers)
                .totalDepartments(totalDepartments)
                .totalCourses(totalCourses)
                .totalSessions(totalSessions)
                .totalQuizzes(totalQuizzes)
                .totalAnnouncements(totalAnnouncements)
                .totalMaterials(totalMaterials)
                .totalMessages(totalMessages)
                .highRiskCount(highRisk)
                .mediumRiskCount(mediumRisk)
                .lowRiskCount(lowRisk)
                .build();
    }

    private AdminChartsDto buildCharts() {
        List<ChartItemDto> usersByRole = List.of(
                new ChartItemDto("Students", userRepository.countByRole(UserRole.STUDENT)),
                new ChartItemDto("Teachers", userRepository.countByRole(UserRole.TEACHER)),
                new ChartItemDto("Admins", userRepository.countByRole(UserRole.ADMIN))
        );

        List<ChartItemDto> contentActivity = List.of(
                new ChartItemDto("Quizzes", teacherQuizRepository.count()),
                new ChartItemDto("Announcements", announcementRepository.count()),
                new ChartItemDto("Materials", learningMaterialRepository.count()),
                new ChartItemDto("Messages", directMessageRepository.count())
        );

        List<ChartItemDto> riskDistribution = List.of(
                new ChartItemDto("High", dropoutPredictionRepository.countByRiskLevel("HIGH")),
                new ChartItemDto("Medium", dropoutPredictionRepository.countByRiskLevel("MEDIUM")),
                new ChartItemDto("Low", dropoutPredictionRepository.countByRiskLevel("LOW"))
        );

        List<ChartItemDto> sessionStatusDistribution = List.of(
                new ChartItemDto("Scheduled", classSessionRepository.countByStatus(ClassSessionStatus.SCHEDULED)),
                new ChartItemDto("Open", classSessionRepository.countByStatus(ClassSessionStatus.OPEN)),
                new ChartItemDto("Completed", classSessionRepository.countByStatus(ClassSessionStatus.COMPLETED)),
                new ChartItemDto("Cancelled", classSessionRepository.countByStatus(ClassSessionStatus.CANCELLED))
        );

        List<ChartItemDto> coursesByDepartment = departmentRepository.findAll()
                .stream()
                .map(dept -> new ChartItemDto(
                        dept.getName(),
                        courseRepository.countByDepartmentId(dept.getId())
                ))
                .toList();

        return AdminChartsDto.builder()
                .usersByRole(usersByRole)
                .contentActivity(contentActivity)
                .riskDistribution(riskDistribution)
                .sessionStatusDistribution(sessionStatusDistribution)
                .coursesByDepartment(coursesByDepartment)
                .build();
    }

    private List<RecentActivityDto> buildRecentActivities() {
        List<RecentActivityDto> items = new ArrayList<>();

        teacherQuizRepository.findTop5ByOrderByCreatedAtDesc()
                .forEach(q -> items.add(mapQuiz(q)));

        announcementRepository.findTop5ByOrderByCreatedAtDesc()
                .forEach(a -> items.add(mapAnnouncement(a)));

        learningMaterialRepository.findTop5ByOrderByCreatedAtDesc()
                .forEach(m -> items.add(mapMaterial(m)));

        directMessageRepository.findTop5ByOrderByCreatedAtDesc()
                .forEach(msg -> items.add(mapMessage(msg)));

        classSessionRepository.findTop5ByOrderByCreatedAtDesc()
                .forEach(session -> items.add(mapSession(session)));

        dropoutPredictionRepository.findTop10ByOrderByPredictedAtDesc()
                .stream()
                .limit(5)
                .forEach(pred -> items.add(mapPrediction(pred)));

        return items.stream()
                .filter(i -> i.getTimestamp() != null)
                .sorted(Comparator.comparing(RecentActivityDto::getTimestamp).reversed())
                .limit(10)
                .toList();
    }

    private RecentActivityDto mapQuiz(TeacherQuiz q) {
        return RecentActivityDto.builder()
                .type("QUIZ_CREATED")
                .title("Quiz created: " + safe(q.getCourseCode()))
                .subtitle(safe(q.getTitle()) + " | Topic: " + safe(q.getTopic()))
                .timestamp(q.getCreatedAt())
                .build();
    }

    private RecentActivityDto mapAnnouncement(CourseAnnouncement a) {
        return RecentActivityDto.builder()
                .type("ANNOUNCEMENT_POSTED")
                .title("Announcement posted")
                .subtitle(safe(a.getTitle()))
                .timestamp(a.getCreatedAt())
                .build();
    }

    private RecentActivityDto mapMaterial(LearningMaterial m) {
        return RecentActivityDto.builder()
                .type("MATERIAL_UPLOADED")
                .title("Material uploaded")
                .subtitle(safe(m.getTitle()) + " | Type: " + (m.getType() != null ? m.getType().name() : "-"))
                .timestamp(m.getCreatedAt())
                .build();
    }

    private RecentActivityDto mapMessage(DirectMessage msg) {
        return RecentActivityDto.builder()
                .type("MESSAGE_SENT")
                .title("Teacher-student message sent")
                .subtitle(safe(msg.getSubject()) + " | To: " + safe(msg.getReceiverName()))
                .timestamp(msg.getCreatedAt())
                .build();
    }

    private RecentActivityDto mapSession(ClassSession s) {
        return RecentActivityDto.builder()
                .type("SESSION_CREATED")
                .title("Class session scheduled")
                .subtitle(safe(s.getCourseCode()) + " | " + s.getSessionDate() + " " + s.getStartTime())
                .timestamp(s.getCreatedAt())
                .build();
    }

    private RecentActivityDto mapPrediction(DropoutPrediction p) {
        return RecentActivityDto.builder()
                .type("PREDICTION_RUN")
                .title("Dropout prediction generated")
                .subtitle(safe(p.getStudentName()) + " | " + safe(p.getCourseCode()) + " | Risk: " + safe(p.getRiskLevel()))
                .timestamp(p.getPredictedAt())
                .build();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}