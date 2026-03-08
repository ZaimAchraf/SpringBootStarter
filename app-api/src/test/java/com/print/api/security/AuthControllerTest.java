package com.print.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.print.api.exception.GlobalExceptionHandler;
import com.print.security.service.AuthService;
import com.print.security.service.PasswordResetService;
import com.print.shared.dto.security.PasswordResetConfirmRequest;
import com.print.shared.dto.security.PasswordResetRequest;
import com.print.shared.dto.user.CreateUserRequest;
import com.print.shared.dto.user.LoginRequest;
import com.print.shared.dto.user.UserDTO;
import com.print.shared.enums.Role;
import com.print.shared.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private PasswordResetService passwordResetService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(authService, passwordResetService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void login_shouldReturnToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("achraf");
        request.setPassword("secret");
        when(authService.login(any(LoginRequest.class), any())).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authService).login(any(LoginRequest.class), any());
    }

    @Test
    void login_shouldReturnBadRequest_whenCredentialsInvalid() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("achraf");
        request.setPassword("bad");
        when(authService.login(any(LoginRequest.class), any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    @Test
    void register_shouldReturnUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("achraf");
        request.setRole(Role.ROLE_CLIENT);

        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setUsername("achraf");
        dto.setEmail("achraf@test.com");
        when(authService.register(any(CreateUserRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("achraf"));
    }

    @Test
    void refresh_shouldReturnToken_whenValidCookie() throws Exception {
        when(authService.refresh(any(), any())).thenReturn(Optional.of("new-jwt"));

        mockMvc.perform(post("/api/auth/refresh").cookie(new jakarta.servlet.http.Cookie("refreshToken", "r1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-jwt"));
    }

    @Test
    void refresh_shouldReturnUnauthorized_whenInvalidCookie() throws Exception {
        when(authService.refresh(any(), any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldReturnOk() throws Exception {
        doNothing().when(authService).logout(any(), any());

        mockMvc.perform(post("/api/auth/logout").cookie(new jakarta.servlet.http.Cookie("refreshToken", "r1")))
                .andExpect(status().isOk());

        verify(authService).logout(any(), any());
    }

    @Test
    void passwordResetRequest_shouldReturnOk() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest();
        request.setEmail("achraf@test.com");
        doNothing().when(passwordResetService).requestReset("achraf@test.com");

        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void passwordResetConfirm_shouldReturnOk() throws Exception {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setToken("tkn");
        request.setNewPassword("NewPass123!");
        doNothing().when(passwordResetService).resetPassword("tkn", "NewPass123!");

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void passwordResetConfirm_shouldReturnInternalServerError_whenTokenInvalid() throws Exception {
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest();
        request.setToken("invalid");
        request.setNewPassword("NewPass123!");
        doThrow(new RuntimeException("Invalid token")).when(passwordResetService)
                .resetPassword("invalid", "NewPass123!");

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"))
                .andExpect(jsonPath("$.details").value("Invalid token"));
    }
}
