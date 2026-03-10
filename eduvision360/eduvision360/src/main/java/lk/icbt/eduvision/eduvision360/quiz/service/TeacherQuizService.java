package lk.icbt.eduvision.eduvision360.quiz.service;

import lk.icbt.eduvision.eduvision360.quiz.dto.*;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface TeacherQuizService {

    GeneratedTeacherQuizResponse generateDraft(TeacherGenerateQuizRequest request, Authentication authentication);

    TeacherQuizResponse saveQuiz(CreateTeacherQuizRequest request, Authentication authentication);

    List<TeacherQuizResponse> getMyQuizzes(Authentication authentication);

    List<TeacherQuizResponse> getMyQuizzesByCourse(String courseId, Authentication authentication);

    TeacherQuizResponse getMyQuizById(String id, Authentication authentication);
}