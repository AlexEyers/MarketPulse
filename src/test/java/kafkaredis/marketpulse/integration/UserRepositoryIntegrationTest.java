package kafkaredis.marketpulse.integration;

import kafkaredis.marketpulse.entity.User;
import kafkaredis.marketpulse.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Uses a real UserRepository with a real temporary PostgreSQL DB
// Proves that Flyway, JPA entity mapping, and Spring Data repository methods work together correctly.
// (Flyway indirectly tested : Must have successfully ran enough migration SQL to create the users table with the columns JPA needs)
class UserRepositoryIntegrationTest extends PostgresIntegrationTest {

    // Real UserRepository bean, so save/find/exists runs real JPA/SQL
    @Autowired
    private UserRepository userRepository;

    // Test that
    @Test
    void saveAndFindUserById() {

        // Proves :
        // - The users table exists
        // - User entity maps to the users table
        // - JPA can insert a row into PostgreSQL
        // - The username/email/password_hash/created_at columns work
        // PostgreSQL/JPA can generate/fill the id
        User savedUser = userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));
        // Proves the generated id can be used to query the same row back.
        Optional<User> result = userRepository.findById(savedUser.getId());
        // Proves the user row was actually persisted and found
        assertTrue(result.isPresent());
        // Proves the saved column values came back correctly from PostgreSQL into the User entity
        assertEquals("user", result.get().getUsername());
        assertEquals("user@example.com", result.get().getEmail());
        assertEquals("hashed-password", result.get().getPasswordHash());
    }

    @Test
    void existsByUsername_returnsTrueWhenUsernameExists() {
        userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));
        // Proves :
        // - Spring Data correctly generated a real query from existsByUsername(...)
        // - The query correctly checks the username column and found the row
        boolean result = userRepository.existsByUsername("user");
        assertTrue(result);
    }

    @Test
    void existsByEmail_returnsTrueWhenEmailExists() {
        userRepository.save(new User(
                "user",
                "user@example.com",
                "hashed-password"
        ));

        boolean result = userRepository.existsByEmail("user@example.com");

        assertTrue(result);
    }
}
