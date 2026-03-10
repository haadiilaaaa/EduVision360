package lk.icbt.eduvision.eduvision360.quiz.controller;

import jakarta.validation.Valid;
import lk.icbt.eduvision.eduvision360.quiz.dto.*;
import lk.icbt.eduvision.eduvision360.quiz.service.TeacherQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/quizzes")
@RequiredArgsConstructor
public class TeacherQuizController {

    private final TeacherQuizService teacherQuizService;

    @PostMapping("/generate")
    public ResponseEntity<GeneratedTeacherQuizResponse> generateDraft(
            @Valid @RequestBody TeacherGenerateQuizRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(teacherQuizService.generateDraft(request, authentication));
    }

    @PostMapping
    public ResponseEntity<TeacherQuizResponse> saveQuiz(
            @Valid @RequestBody CreateTeacherQuizRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(201).body(teacherQuizService.saveQuiz(request, authentication));
    }

    @GetMapping
    public ResponseEntity<List<TeacherQuizResponse>> getMyQuizzes(Authentication authentication) {
        return ResponseEntity.ok(teacherQuizService.getMyQuizzes(authentication));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<TeacherQuizResponse>> getMyQuizzesByCourse(
            @PathVariable String courseId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(teacherQuizService.getMyQuizzesByCourse(courseId, authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherQuizResponse> getMyQuizById(
            @PathVariable String id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(teacherQuizService.getMyQuizById(id, authentication));
    }
}