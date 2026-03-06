package lk.icbt.eduvision.eduvision360.ai.service;

import lk.icbt.eduvision.eduvision360.ai.dto.*;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface AiTutorService {

    AiTextResponse askChatbot(AskAiRequest request, Authentication authentication);

    AiTextResponse askTutor(AskAiRequest request, Authentication authentication);

    AiTextResponse generateSummary(GenerateSummaryRequest request, Authentication authentication);

    AiQuizResponse generateQuiz(GenerateQuizRequest request, Authentication authentication);

    List<AiTextResponse> getMyAiHistory(Authentication authentication);
}