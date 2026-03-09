package com.tottrust.starter.security.service;

import com.tottrust.starter.security.entity.RefreshToken;
import com.tottrust.starter.security.jwt.JwtService;
import com.tottrust.starter.security.repository.RefreshTokenRepository;
import com.tottrust.starter.shared.dto.security.RefreshResponse;
import com.tottrust.starter.shared.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final UserDetailsService userDetailsService;
    private final long refreshTokenDurationDays = 30;
    private final JwtService jwtService;

    public RefreshTokenService(RefreshTokenRepository repo, UserDetailsService userDetailsService, JwtService jwtService) {
        this.repo = repo;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    public RefreshToken createOrReuseRefreshToken(String username) {

        return repo.findByUsername(username)
                .filter(rt -> rt.getExpiryDate().isAfter(Instant.now()))
                .orElseGet(() -> {
                    repo.deleteByUsername(username);

                    RefreshToken rt = new RefreshToken();
                    rt.setToken(UUID.randomUUID().toString());
                    rt.setUsername(username);
                    rt.setExpiryDate(
                            Instant.now().plus(refreshTokenDurationDays, ChronoUnit.DAYS)
                    );
                    return repo.save(rt);
                });
    }

    @Transactional
    public Optional<RefreshResponse> refreshAccessToken(String refreshTokenValue, boolean rotate) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) return Optional.empty();

        Optional<RefreshToken> maybe = repo.findByToken(refreshTokenValue);
        if (maybe.isEmpty()) return Optional.empty();

        RefreshToken rt = maybe.get();

        log.info("expiration : {}, current date : {}", rt.getExpiryDate(), Instant.now());

        if (rt.getExpiryDate().isBefore(Instant.now())) {
            repo.delete(rt);
            return Optional.empty();
        }

        String username = rt.getUsername();

        UserDetails userDetails = userDetailsService.loadUserByUsername(rt.getUsername());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // Génère un nouvel access token avec le rôle actuel
        String newAccessToken = jwtService.generateToken(username, roles);

        String newRefreshTokenValue = null;
        if (rotate) {
            repo.delete(rt);
            RefreshToken newRt = new RefreshToken();
            newRt.setToken(UUID.randomUUID().toString());
            newRt.setUsername(username);
            newRt.setExpiryDate(Instant.now().plus(refreshTokenDurationDays, ChronoUnit.DAYS));
            repo.save(newRt);
            newRefreshTokenValue = newRt.getToken();
        }

        return Optional.of(new RefreshResponse(newAccessToken, newRefreshTokenValue));
    }



    public Optional<RefreshToken> findByToken(String token) {
        return repo.findByToken(token);
    }

    public boolean isExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(Instant.now());
    }

    public void deleteByToken(String token) {
        repo.deleteByToken(token);
    }

    public void deleteByUsername(String username) {
        repo.deleteByUsername(username);
    }
}
