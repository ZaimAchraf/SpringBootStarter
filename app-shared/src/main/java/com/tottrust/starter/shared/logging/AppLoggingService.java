package com.tottrust.starter.shared.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppLoggingService {

    private static final Logger auditLog = LoggerFactory.getLogger("APP_AUDIT");

    public void info(String event, Map<String, Object> context) {
        auditLog.info("{} {}", event, formatContext(context));
    }

    public void warn(String event, Map<String, Object> context) {
        auditLog.warn("{} {}", event, formatContext(context));
    }

    public void error(String event, Map<String, Object> context, Throwable throwable) {
        auditLog.error("{} {}", event, formatContext(context), throwable);
    }

    private String formatContext(Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return "";
        }

        return context.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + String.valueOf(entry.getValue()))
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
