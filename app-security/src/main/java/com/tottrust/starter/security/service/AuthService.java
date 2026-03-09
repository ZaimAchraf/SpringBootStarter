package com.tottrust.starter.security.service;

import com.tottrust.starter.security.entity.RefreshToken;
import com.tottrust.starter.security.jwt.JwtService;
import com.tottrust.starter.shared.dto.security.RefreshResponse;
import com.tottrust.starter.shared.dto.user.CreateUserRequest;
import com.tottrust.starter.shared.dto.user.LoginRequest;
import com.tottrust.starter.shared.dto.user.UserDTO;
import com.tottrust.starter.shared.enums.Role;
import com.tottrust.starter.shared.exception.InvalidCredentialsException;
import com.tottrust.starter.shared.logging.AppLoggingService;
import com.tottrust.starter.user.service.IUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final IUserService userService;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AppLoggingService appLoggingService;

    private long refreshTokenDurationDays = 30;


    @Value("${security.cookie.secure}")
    private Boolean secureCookie;

    @Value("${security.cookie.same-site}")
    private String SameSiteCookie;

    public String login(LoginRequest request, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            appLoggingService.warn("auth.login.failed", Map.of("username", request.getUsername()));
            throw new InvalidCredentialsException();
        }

        appLoggingService.info("auth.login.success", Map.of("username", request.getUsername()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // create refresh token
        RefreshToken refreshToken = refreshTokenService.createOrReuseRefreshToken(request.getUsername());

        // set cookie (HttpOnly)
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true)
                .secure(secureCookie) // true en prod (HTTPS)
                .path("/")    // accessible pour /api/auth/refresh
                .maxAge(60 * 60 * 24 * 30) // 30 days
                .sameSite(SameSiteCookie)
                .build();
        response.setHeader("Set-Cookie", cookie.toString());

        return jwtService.generateToken(request.getUsername(), roles);
    }

    public Optional<String> refresh(
            String refreshTokenCookie,
            HttpServletResponse response
    ) {

        log.info("received cookie : {}", refreshTokenCookie);

        Optional<RefreshResponse> maybe =
                refreshTokenService.refreshAccessToken(refreshTokenCookie, true); // rotate

        if (maybe.isEmpty()) {
            return Optional.empty();
        }

        RefreshResponse refreshResp = maybe.get();

        // rotation → update cookie
        if (refreshResp.refreshTokenIfRotated() != null) {

            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshResp.refreshTokenIfRotated())
                    .httpOnly(true)
                    .secure(secureCookie)
                    .sameSite(SameSiteCookie)
                    .path("/")
                    .maxAge(refreshTokenDurationDays * 24 * 60 * 60)
                    .build();

            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        appLoggingService.info("auth.refresh.success", Map.of("hasRotation", refreshResp.refreshTokenIfRotated() != null));
        return Optional.of(refreshResp.accessToken());
    }


    public void logout(String refreshTokenCookie, HttpServletResponse response) {

        if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
            refreshTokenService.deleteByToken(refreshTokenCookie);
        }

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(SameSiteCookie)
                .path("/")
                .maxAge(0) // suppression immédiate
                .build();

        response.setHeader("Set-Cookie", deleteCookie.toString());
        appLoggingService.info("auth.logout.success", Map.of("hadRefreshCookie", refreshTokenCookie != null && !refreshTokenCookie.isBlank()));
    }


    public UserDTO register(CreateUserRequest request) {

        if (request.getRole() != null && request.getRole() != Role.ROLE_CLIENT) {
            log.error("Tentative d'inscription avec un rôle interdit : {}", request.getRole());
            throw new RuntimeException("Action forbidden : rôle non autorisé") ;
        }

        return userService.createUser(request);
    }
}
