package lk.icbt.eduvision.eduvision360.course.controller;

import jakarta.validation.Valid;
import lk.icbt.eduvision.eduvision360.course.dto.CreateCourseRequest;
import lk.icbt.eduvision.eduvision360.course.dto.CourseResponse;
import lk.icbt.eduvision.eduvision360.course.dto.UpdateCourseRequest;
import lk.icbt.eduvision.eduvision360.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CreateCourseRequest req) {
        return ResponseEntity.status(201).body(courseService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<CourseResponse>> getAll() {
        return ResponseEntity.ok(courseService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(courseService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateCourseRequest req
    ) {
        return ResponseEntity.ok(courseService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id) {
        courseService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Course deleted successfully"));
    }

    @GetMapping("/teacher/my")
    public ResponseEntity<List<CourseResponse>> getMyCourses(Authentication authentication) {
        return ResponseEntity.ok(courseService.getMyCourses(authentication.getName()));
    }

    @GetMapping("/student/available")
    public ResponseEntity<List<CourseResponse>> availableForStudents() {
        return ResponseEntity.ok(courseService.getAll()); // later you can filter ACTIVE only
    }
}