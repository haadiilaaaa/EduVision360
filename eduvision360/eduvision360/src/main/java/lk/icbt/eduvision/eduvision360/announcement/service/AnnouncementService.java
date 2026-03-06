package lk.icbt.eduvision.eduvision360.announcement.service;

import lk.icbt.eduvision.eduvision360.announcement.dto.AnnouncementResponse;
import lk.icbt.eduvision.eduvision360.announcement.dto.CreateAnnouncementRequest;

import java.util.List;

public interface AnnouncementService {

    AnnouncementResponse create(CreateAnnouncementRequest req, String teacherId);

    AnnouncementResponse updateTeacherAnnouncement(String teacherId, String announcementId, CreateAnnouncementRequest req);

    List<AnnouncementResponse> getTeacherAnnouncements(String teacherId);

    List<AnnouncementResponse> getTeacherAnnouncementsByCourse(String teacherId, String courseId);

    void deleteTeacherAnnouncement(String teacherId, String announcementId);

    List<AnnouncementResponse> getStudentAnnouncementsForCourse(String studentId, String courseId);

    List<AnnouncementResponse> getStudentAnnouncementsForMyCourses(String studentId);
}