package com.print.api.security;

import com.print.security.service.AuthService;
import com.print.security.service.PasswordResetService;
import com.print.shared.dto.security.PasswordResetConfirmRequest;
import com.print.shared.dto.security.PasswordResetRequest;
import com.print.shared.dto.user.AuthResponse;
import com.print.shared.dto.user.CreateUserRequest;
import com.print.shared.dto.user.LoginRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService resetService;

    /**
     * Endpoint pour authentification (login)
     * @param request : contient username et password
     * @return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        log.info("Login request received for username={}", request.getUsername());
        String token = authService.login(request, response);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    /**
     * Endpoint pour créer un nouvel utilisateur
     * @param request : CreateUserRequest avec infos user
     * @return User créé
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CreateUserRequest request) {
        var user = authService.register(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            HttpServletResponse response
    ) {

        return authService.refresh(refreshTokenCookie, response)
                .<ResponseEntity<?>>map(token ->
                        ResponseEntity.ok(Map.of("token", token))
                )
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body("Refresh token invalid or expired")
                );
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
                                    HttpServletResponse response) {
        authService.logout(refreshTokenCookie, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<?> request(@RequestBody PasswordResetRequest request) {
        resetService.requestReset(request.getEmail());
        return ResponseEntity.ok().build();
    }

    /**
     * Étape 2 : confirmation reset
     */
    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirm(@RequestBody PasswordResetConfirmRequest request) {
        resetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

}
