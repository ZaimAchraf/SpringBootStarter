package com.print.security.service;

import com.print.mail.order.EmailService;
import com.print.shared.logging.AppLoggingService;
import com.print.user.entity.PasswordResetToken;
import com.print.user.entity.User;
import com.print.user.repository.PasswordResetTokenRepository;
import com.print.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AppLoggingService appLoggingService;

    @Value("${frontend.base-url}")
    private String baseFrontendUrl;

    private final Duration TOKEN_VALIDITY = Duration.ofMinutes(30);

    public void requestReset(String email) {

        userRepository.findByEmail(email).ifPresent(user -> {

            String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
            String tokenHash = hash(rawToken);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(tokenHash)
                    .expiresAt(LocalDateTime.now().plus(TOKEN_VALIDITY))
                    .used(false)
                    .build();

            tokenRepository.save(resetToken);


            String resetLink = baseFrontendUrl + "/auth/reset-password?token=" + rawToken;

            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getName(),
                    resetLink
            );

            appLoggingService.info("auth.password-reset.requested", java.util.Map.of("email", user.getEmail()));
        });
    }

    public void resetPassword(String token, String newPassword) {

        String tokenHash = hash(token);

        PasswordResetToken resetToken = tokenRepository
                .findByTokenHashAndUsedFalse(tokenHash)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        appLoggingService.info("auth.password-reset.completed", java.util.Map.of("userId", user.getId()));
    }

    private String hash(String raw) {
        return DigestUtils.sha256Hex(raw);
    }
}
