package lk.icbt.eduvision.eduvision360.classsession.service;

import lk.icbt.eduvision.eduvision360.classsession.dto.ClassSessionResponse;
import lk.icbt.eduvision.eduvision360.classsession.dto.CreateClassSessionRequest;
import lk.icbt.eduvision.eduvision360.classsession.dto.UpdateClassSessionStatusRequest;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ClassSessionService {

    ClassSessionResponse createSession(CreateClassSessionRequest request, Authentication authentication);

    List<ClassSessionResponse> getTeacherSessions(Authentication authentication);

    List<ClassSessionResponse> getStudentSessions(Authentication authentication);

    ClassSessionResponse updateSessionStatus(
            String sessionId,
            UpdateClassSessionStatusRequest request,
            Authentication authentication
    );
}