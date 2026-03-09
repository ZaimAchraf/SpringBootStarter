package com.tottrust.starter.shared.dto.session;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserSessionStore {
    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();


}
