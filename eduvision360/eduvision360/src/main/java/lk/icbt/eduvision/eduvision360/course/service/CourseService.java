package lk.icbt.eduvision.eduvision360.course.service;

import lk.icbt.eduvision.eduvision360.course.dto.CreateCourseRequest;
import lk.icbt.eduvision.eduvision360.course.dto.CourseResponse;
import lk.icbt.eduvision.eduvision360.course.dto.UpdateCourseRequest;

import java.util.List;

public interface CourseService {

    CourseResponse create(CreateCourseRequest req);

    List<CourseResponse> getAll();

    CourseResponse getById(String id);

    CourseResponse update(String id, UpdateCourseRequest req);

    void delete(String id);

    List<CourseResponse> getMyCourses(String teacherId);
}