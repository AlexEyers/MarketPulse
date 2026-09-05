package kafkaredis.marketpulse.controller;

import kafkaredis.marketpulse.dto.UserDTO;
import kafkaredis.marketpulse.exception.EmailAlreadyExistsException;
import kafkaredis.marketpulse.exception.GlobalExceptionHandler;
import kafkaredis.marketpulse.exception.UsernameAlreadyExistsException;
import kafkaredis.marketpulse.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);

    private final MockMvc mockMvc = standaloneSetup(new UserController(userService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    // Test that POST /api/users returns 201 Created with the created user
    @Test
    void createUser_returnsCreatedUser() throws Exception {
        when(userService.createUser(any()))
                .thenReturn(new UserDTO(1L, "user", "user@example.com", Instant.parse("2026-09-05T10:00:00Z")));

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "user",
                                  "email": "user@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    // Test that a duplicate username returns a 409 Conflict error response
    @Test
    void createUser_returns409WhenUsernameAlreadyExists() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new UsernameAlreadyExistsException("Username is taken"));

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "user",
                                  "email": "user@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("USERNAME_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Username is taken"));
    }

    // Test that a duplicate email returns a 409 Conflict error response
    @Test
    void createUser_returns409WhenEmailAlreadyExists() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new EmailAlreadyExistsException("Email is in use"));

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "user",
                                  "email": "user@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Email is in use"));
    }

    // Test that a blank username returns 400 before the service is called
    @Test
    void createUser_returns400WhenUsernameIsBlank() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "",
                                  "email": "user@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(userService, never()).createUser(any());
    }

    // Test that an invalid email returns 400 before the service is called
    @Test
    void createUser_returns400WhenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "user",
                                  "email": "not-an-email",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(userService, never()).createUser(any());
    }

    // Test that a blank password returns 400 before the service is called
    @Test
    void createUser_returns400WhenPasswordIsBlank() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "username": "user",
                                  "email": "user@example.com",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(userService, never()).createUser(any());
    }
}
