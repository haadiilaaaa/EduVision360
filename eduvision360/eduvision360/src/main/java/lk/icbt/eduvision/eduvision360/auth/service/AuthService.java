package lk.icbt.eduvision.eduvision360.auth.service;

import lk.icbt.eduvision.eduvision360.auth.dto.LoginRequest;
import lk.icbt.eduvision.eduvision360.auth.dto.RegisterRequest;
import lk.icbt.eduvision.eduvision360.auth.dto.VerifyOtpRequest;

public interface AuthService {

    void register(RegisterRequest req);

    void verifyOtp(VerifyOtpRequest req);
    void resendRegisterOtp(String email);

    // ✅ MUST MATCH IMPLEMENTATION
    String login(LoginRequest req);
}
