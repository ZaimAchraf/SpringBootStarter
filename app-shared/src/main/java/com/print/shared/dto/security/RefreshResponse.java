package com.print.shared.dto.security;

public record RefreshResponse(String accessToken, String refreshTokenIfRotated) { }
