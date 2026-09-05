package kafkaredis.marketpulse.service;

import kafkaredis.marketpulse.dto.CreateUserRequestDTO;
import kafkaredis.marketpulse.dto.UserDTO;
import kafkaredis.marketpulse.entity.User;
import kafkaredis.marketpulse.exception.EmailAlreadyExistsException;
import kafkaredis.marketpulse.exception.UsernameAlreadyExistsException;
import kafkaredis.marketpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDTO createUser(CreateUserRequestDTO request) {

        // Normalise username + email
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();

        // Throw errors if username/email are in use
        if(userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException("Username is taken");
        }

        if(userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email is in use");
        }

        // Hash password
        String passwordHash = passwordEncoder.encode(request.password());

        // Save user to DB
        User savedUser = userRepository.save(new User(username, email, passwordHash));

        return toDTO(savedUser);
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
