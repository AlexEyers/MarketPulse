package kafkaredis.marketpulse.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity // JPA entity, maps to a DB table
@Table(name = "users") // Entity maps to "users" table
@Getter
@NoArgsConstructor // Required by JPA to create User objects when reading rows from the database
public class User {

    @Id // Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto increment ID
    private Long id;

    @Column(nullable = false, unique = true, length = 50) // Map to username column, required field, unique and max length of 50
    private String username;

    @Column(nullable = false, unique = true, length = 255) // Required field
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255) // Database value is password_hash
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false) // Database value is created_at
    private Instant createdAt;

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = Instant.now();
    }
}
