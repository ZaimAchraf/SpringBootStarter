package com.tottrust.starter.api.security;

import com.tottrust.starter.api.exception.GlobalExceptionHandler;
import com.tottrust.starter.api.user.UserController;
import com.tottrust.starter.security.config.CorsProperties;
import com.tottrust.starter.security.config.SecurityConfig;
import com.tottrust.starter.security.filter.JwtFilter;
import com.tottrust.starter.security.service.AuthService;
import com.tottrust.starter.security.service.PasswordResetService;
import com.tottrust.starter.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = SecurityAccessTest.TestApplication.class,
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
        }
)
@AutoConfigureMockMvc
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;
    @MockBean
    private PasswordResetService passwordResetService;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtFilter jwtFilter;
    @MockBean
    private CorsProperties corsProperties;

    @BeforeEach
    void setUp() throws Exception {
        when(corsProperties.getAllowedOrigins()).thenReturn(List.of("http://localhost:4200"));
        when(corsProperties.getAllowedMethods()).thenReturn(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        when(corsProperties.getAllowedHeaders()).thenReturn(List.of("*"));
        when(corsProperties.getExposedHeaders()).thenReturn(List.of("Authorization", "Content-Type"));
        when(corsProperties.isAllowCredentials()).thenReturn(true);

        doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtFilter).doFilter(any(), any(), any());
    }

    @Test
    void authRoutes_shouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType("application/json")
                        .content("{\"email\":\"achraf@test.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void usersMe_shouldPassSecurityButReturnUnauthorizedFromController_whenNoAuthPrincipal() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usersRoute_shouldBeForbiddenWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void usersRoute_shouldBeForbiddenForClientRole() throws Exception {
        mockMvc.perform(get("/api/users")
                        .with(SecurityMockMvcRequestPostProcessors.user("client").roles("CLIENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void usersRoute_shouldBeAllowedForAdminRole() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(com.tottrust.starter.user.entity.User.builder()
                .id(1L)
                .username("achraf")
                .name("Achraf")
                .email("achraf@test.com")
                .role(com.tottrust.starter.shared.enums.Role.ROLE_ADMIN)
                .build()));

        mockMvc.perform(get("/api/users")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({AuthController.class, UserController.class, SecurityConfig.class, GlobalExceptionHandler.class})
    static class TestApplication {
    }
}
