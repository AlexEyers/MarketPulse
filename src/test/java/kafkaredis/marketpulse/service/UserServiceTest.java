package kafkaredis.marketpulse.service;

import kafkaredis.marketpulse.dto.CreateUserRequestDTO;
import kafkaredis.marketpulse.dto.UserDTO;
import kafkaredis.marketpulse.entity.User;
import kafkaredis.marketpulse.exception.EmailAlreadyExistsException;
import kafkaredis.marketpulse.exception.UsernameAlreadyExistsException;
import kafkaredis.marketpulse.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private final UserService userService = new UserService(userRepository, passwordEncoder);

    @Test
    void createUser_trimsUsernameLowercasesEmailHashesPasswordAndSavesUser() {

        CreateUserRequestDTO request = new CreateUserRequestDTO(
                " user ",
                " USER@EXAMPLE.COM ",
                "password123"
        );
        // Mock that the username and email is unique, and pretend to hash password when called.
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        // Return the same object User that was saved
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Create the request
        UserDTO result = userService.createUser(request);
        // Assert saved user had username = user and email = user@example.com
        assertEquals("user", result.username());
        assertEquals("user@example.com", result.email());
        // Verify that the service checked for unique username, email, hashed the password and saved a User object
        verify(userRepository).existsByUsername("user");
        verify(userRepository).existsByEmail("user@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_savesHashedPasswordNotRawPassword() {
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "user",
                "user@example.com",
                "password123"
        );

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.createUser(request);
        // Create a captor that can capture User arguments
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        // Capture argument passed into .save()
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("hashed-password", savedUser.getPasswordHash());
    }

    @Test
    void createUser_returnsUserDTOWithoutPassword() {
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "user",
                "user@example.com",
                "password123"
        );

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.createUser(request);

        assertEquals("user", result.username());
        assertEquals("user@example.com", result.email());
    }

    @Test
    void createUser_throwsWhenUsernameAlreadyExists() {
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "user",
                "user@example.com",
                "password123"
        );

        when(userRepository.existsByUsername("user")).thenReturn(true);

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        verify(userRepository, never()).existsByEmail(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_throwsWhenEmailAlreadyExists() {
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                "user",
                "user@example.com",
                "password123"
        );

        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.createUser(request)
        );

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
    }
}
