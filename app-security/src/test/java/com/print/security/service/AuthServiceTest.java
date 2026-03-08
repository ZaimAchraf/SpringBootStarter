package com.print.security.service;

import com.print.security.entity.RefreshToken;
import com.print.security.jwt.JwtService;
import com.print.shared.dto.security.RefreshResponse;
import com.print.shared.dto.user.CreateUserRequest;
import com.print.shared.dto.user.LoginRequest;
import com.print.shared.dto.user.UserDTO;
import com.print.shared.enums.Role;
import com.print.shared.exception.InvalidCredentialsException;
import com.print.shared.logging.AppLoggingService;
import com.print.user.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private IUserService userService;
    @Mock
    private UserDetailsServiceImpl userDetailsService;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AppLoggingService appLoggingService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "secureCookie", false);
        ReflectionTestUtils.setField(authService, "SameSiteCookie", "Lax");
    }

    @Test
    void login_shouldReturnJwtAndSetRefreshCookie() {
        LoginRequest request = new LoginRequest();
        request.setUsername("achraf");
        request.setPassword("secret");

        UserDetails details = User.withUsername("achraf")
                .password("encoded")
                .authorities("ROLE_ADMIN")
                .build();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-123");

        when(userDetailsService.loadUserByUsername("achraf")).thenReturn(details);
        when(refreshTokenService.createOrReuseRefreshToken("achraf")).thenReturn(refreshToken);
        when(jwtService.generateToken("achraf", List.of("ROLE_ADMIN"))).thenReturn("jwt-123");

        MockHttpServletResponse response = new MockHttpServletResponse();
        String token = authService.login(request, response);

        assertEquals("jwt-123", token);
        assertTrue(response.getHeader("Set-Cookie").contains("refreshToken=refresh-123"));
    }

    @Test
    void login_shouldThrowInvalidCredentials_whenAuthenticationFails() {
        LoginRequest request = new LoginRequest();
        request.setUsername("achraf");
        request.setPassword("bad");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad creds"));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request, new MockHttpServletResponse()));
    }

    @Test
    void refresh_shouldReturnAccessTokenAndRotateCookie() {
        when(refreshTokenService.refreshAccessToken("r1", true))
                .thenReturn(Optional.of(new RefreshResponse("jwt-new", "refresh-new")));

        MockHttpServletResponse response = new MockHttpServletResponse();
        Optional<String> refreshed = authService.refresh("r1", response);

        assertTrue(refreshed.isPresent());
        assertEquals("jwt-new", refreshed.get());
        assertTrue(response.getHeader("Set-Cookie").contains("refreshToken=refresh-new"));
    }

    @Test
    void refresh_shouldReturnEmpty_whenRefreshInvalid() {
        when(refreshTokenService.refreshAccessToken("invalid", true)).thenReturn(Optional.empty());

        Optional<String> refreshed = authService.refresh("invalid", new MockHttpServletResponse());

        assertTrue(refreshed.isEmpty());
    }

    @Test
    void logout_shouldDeleteTokenAndSetCookieToExpired() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        authService.logout("refresh-123", response);

        verify(refreshTokenService).deleteByToken("refresh-123");
        assertTrue(response.getHeader("Set-Cookie").contains("Max-Age=0"));
    }

    @Test
    void logout_shouldNotDeleteToken_whenCookieMissing() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        authService.logout(null, response);

        assertTrue(response.getHeader("Set-Cookie").contains("Max-Age=0"));
    }

    @Test
    void register_shouldThrow_whenRoleIsNotClient() {
        CreateUserRequest request = new CreateUserRequest();
        request.setRole(Role.ROLE_ADMIN);

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    @Test
    void register_shouldDelegateToUserService_whenRoleClient() {
        CreateUserRequest request = new CreateUserRequest();
        request.setRole(Role.ROLE_CLIENT);
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        when(userService.createUser(request)).thenReturn(dto);

        UserDTO result = authService.register(request);

        assertEquals(1L, result.getId());
        verify(userService).createUser(eq(request));
    }
}
