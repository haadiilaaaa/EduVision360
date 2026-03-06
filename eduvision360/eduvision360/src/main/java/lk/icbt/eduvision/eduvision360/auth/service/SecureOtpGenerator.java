package lk.icbt.eduvision.eduvision360.auth.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class SecureOtpGenerator implements OtpGenerator {

    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate6Digits() {
        int n = random.nextInt(900000) + 100000; // 100000..999999
        return String.valueOf(n);
    }
}
