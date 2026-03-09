package com.tottrust.starter.shared.dto.security;

public record RefreshResponse(String accessToken, String refreshTokenIfRotated) { }
