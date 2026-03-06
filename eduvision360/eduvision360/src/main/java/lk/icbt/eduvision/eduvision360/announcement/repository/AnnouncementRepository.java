package lk.icbt.eduvision.eduvision360.announcement.repository;

import lk.icbt.eduvision.eduvision360.announcement.model.CourseAnnouncement;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AnnouncementRepository extends MongoRepository<CourseAnnouncement, String> {

    List<CourseAnnouncement> findByTeacherIdOrderByCreatedAtDesc(String teacherId);

    List<CourseAnnouncement> findByTeacherIdAndCourseIdOrderByCreatedAtDesc(String teacherId, String courseId);

    List<CourseAnnouncement> findByCourseIdOrderByCreatedAtDesc(String courseId);

    List<CourseAnnouncement> findByCourseIdInOrderByCreatedAtDesc(List<String> courseIds);

    Optional<CourseAnnouncement> findByIdAndTeacherId(String id, String teacherId);
}