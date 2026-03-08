package com.print.security.service;

import com.print.mail.order.EmailService;
import com.print.shared.logging.AppLoggingService;
import com.print.user.entity.PasswordResetToken;
import com.print.user.entity.User;
import com.print.user.repository.PasswordResetTokenRepository;
import com.print.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private AppLoggingService appLoggingService;

    @InjectMocks
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseFrontendUrl", "http://localhost:4200");
    }

    @Test
    void requestReset_shouldSaveTokenAndSendEmail_whenUserExists() {
        User user = User.builder().id(1L).name("Achraf").email("achraf@test.com").build();
        when(userRepository.findByEmail("achraf@test.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.requestReset("achraf@test.com");

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void requestReset_shouldDoNothing_whenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        service.requestReset("missing@test.com");

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void resetPassword_shouldUpdatePasswordAndMarkTokenUsed_whenTokenIsValid() {
        User user = User.builder().id(1L).password("old").build();
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        when(tokenRepository.findByTokenHashAndUsedFalse(anyString())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NewPass123!")).thenReturn("encoded-pass");

        service.resetPassword("raw-token", "NewPass123!");

        assertEquals("encoded-pass", user.getPassword());
        assertTrue(token.isUsed());
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void resetPassword_shouldThrow_whenTokenInvalid() {
        when(tokenRepository.findByTokenHashAndUsedFalse(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.resetPassword("bad-token", "x"));

        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    void resetPassword_shouldThrow_whenTokenExpired() {
        PasswordResetToken token = PasswordResetToken.builder()
                .user(User.builder().id(1L).build())
                .used(false)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();
        when(tokenRepository.findByTokenHashAndUsedFalse(anyString())).thenReturn(Optional.of(token));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.resetPassword("expired", "x"));

        assertEquals("Token expired", exception.getMessage());
    }
}
