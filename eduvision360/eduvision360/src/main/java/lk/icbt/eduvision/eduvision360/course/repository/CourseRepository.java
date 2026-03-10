package lk.icbt.eduvision.eduvision360.course.repository;

import lk.icbt.eduvision.eduvision360.course.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends MongoRepository<Course, String> {

    boolean existsByCourseCode(String courseCode);

    List<Course> findByTeacherId(String teacherId);

    Optional<Course> findByCourseCodeIgnoreCase(String courseCode);


    java.util.Optional<Course> findByIdAndTeacherId(String id, String teacherId);
    long countByDepartmentId(String departmentId);
}