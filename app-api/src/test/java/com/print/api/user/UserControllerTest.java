package com.print.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.print.api.exception.GlobalExceptionHandler;
import com.print.shared.dto.user.CreateUserRequest;
import com.print.shared.dto.user.UpdateUserRequest;
import com.print.shared.dto.user.UserDTO;
import com.print.shared.enums.Role;
import com.print.user.entity.User;
import com.print.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllUsers_shouldReturnMappedUsers() throws Exception {
        User user = User.builder()
                .id(1L)
                .username("achraf")
                .name("Achraf Zaim")
                .email("achraf@test.com")
                .role(Role.ROLE_ADMIN)
                .build();
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("achraf"))
                .andExpect(jsonPath("$[0].role").value("ROLE_ADMIN"));
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("new-user");
        request.setRole(Role.ROLE_CLIENT);

        UserDTO dto = new UserDTO();
        dto.setId(3L);
        dto.setUsername("new-user");
        dto.setEmail("new@test.com");
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.username").value("new-user"));
    }

    @Test
    void getMe_shouldReturnUnauthorized_whenAuthenticationMissing() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_shouldReturnUser_whenAuthenticated() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setUsername("achraf");
        when(userService.getCurrentUser("achraf")).thenReturn(dto);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "achraf",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        mockMvc.perform(get("/api/users/me").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("achraf"));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setUsername("achraf");
        when(userService.getUserById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getUserById_shouldReturnInternalServerError_whenMissing() throws Exception {
        when(userService.getUserById(99L)).thenThrow(new RuntimeException("User not found with id : 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.details").value("User not found with id : 99"));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated");
        request.setRole("ROLE_ADMIN");
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setName("Updated");
        dto.setRole("ROLE_ADMIN");
        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(dto);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(userService).updateUser(eq(1L), any(UpdateUserRequest.class));
    }
}
