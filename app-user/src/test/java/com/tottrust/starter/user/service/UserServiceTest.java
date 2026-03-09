package com.tottrust.starter.user.service;

import com.tottrust.starter.shared.dto.user.CreateUserRequest;
import com.tottrust.starter.shared.dto.user.UpdateUserRequest;
import com.tottrust.starter.shared.dto.user.UserDTO;
import com.tottrust.starter.shared.enums.Role;
import com.tottrust.starter.shared.exception.UserAlreadyExistsException;
import com.tottrust.starter.user.entity.User;
import com.tottrust.starter.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void cleanSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUser_shouldCreateUser() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Achraf");
        request.setUsername("achraf");
        request.setEmail("achraf@test.com");
        request.setPassword("secret");
        request.setRole(Role.ROLE_CLIENT);

        when(userRepository.findByUsername("achraf")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserDTO dto = userService.createUser(request);

        assertEquals(1L, dto.getId());
        assertEquals("achraf", dto.getUsername());
        assertEquals("achraf@test.com", dto.getEmail());
    }

    @Test
    void createUser_shouldThrow_whenUsernameAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("achraf");
        when(userRepository.findByUsername("achraf")).thenReturn(Optional.of(User.builder().build()));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(request));
    }

    @Test
    void getUserById_shouldReturnUserDto() {
        User user = User.builder().id(1L).username("achraf").email("achraf@test.com").role(Role.ROLE_CLIENT).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO dto = userService.getUserById(1L);

        assertEquals("achraf", dto.getUsername());
    }

    @Test
    void getUserById_shouldThrow_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.getUserById(99L));
        assertEquals("User not found with id : 99", exception.getMessage());
    }

    @Test
    void updateUser_shouldUpdateWithPassword_whenProvided() {
        User existing = User.builder().id(1L).password("old").role(Role.ROLE_CLIENT).build();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("New Name");
        request.setEmail("new@test.com");
        request.setPhone("+212");
        request.setGender("MALE");
        request.setAddress("Casablanca");
        request.setRole("ROLE_ADMIN");
        request.setPassword("new-pass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("new-pass")).thenReturn("encoded-new");
        when(userRepository.save(existing)).thenReturn(existing);

        UserDTO dto = userService.updateUser(1L, request);

        assertEquals("New Name", dto.getName());
        assertEquals("ROLE_ADMIN", dto.getRole());
        assertEquals("encoded-new", existing.getPassword());
    }

    @Test
    void updateUser_shouldNotUpdatePassword_whenBlank() {
        User existing = User.builder().id(1L).password("old").role(Role.ROLE_CLIENT).build();
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("New Name");
        request.setEmail("new@test.com");
        request.setPhone("+212");
        request.setGender("MALE");
        request.setAddress("Casablanca");
        request.setRole("ROLE_CLIENT");
        request.setPassword("  ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        userService.updateUser(1L, request);

        assertEquals("old", existing.getPassword());
    }

    @Test
    void getCurrentUserByUsername_shouldReturnDto() {
        User user = User.builder().id(1L).username("achraf").email("achraf@test.com").role(Role.ROLE_ADMIN).build();
        when(userRepository.findByUsername("achraf")).thenReturn(Optional.of(user));

        UserDTO dto = userService.getCurrentUser("achraf");

        assertEquals("achraf", dto.getUsername());
    }

    @Test
    void getCurrentUserByUsername_shouldThrow_whenMissing() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getCurrentUser("missing"));
    }

    @Test
    void getAllUsers_shouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(User.builder().id(1L).build()));

        List<User> users = userService.getAllUsers();

        assertEquals(1, users.size());
    }

    @Test
    void getCurrentUser_shouldReturnNull_whenNoAuthentication() {
        User current = userService.getCurrentUser();
        assertNull(current);
    }

    @Test
    void getCurrentUser_shouldReturnUser_whenAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "achraf",
                        "n/a",
                        java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
        User user = User.builder().id(1L).username("achraf").build();
        when(userRepository.findByUsername("achraf")).thenReturn(Optional.of(user));

        User current = userService.getCurrentUser();

        assertNotNull(current);
        assertEquals("achraf", current.getUsername());
        verify(userRepository).findByUsername("achraf");
    }
}
