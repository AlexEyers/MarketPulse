package kafkaredis.marketpulse.repository;

import kafkaredis.marketpulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Check if a user row exists where username = x
    boolean existsByUsername(String username);

    // Check if a user row exists where email = x
    boolean existsByEmail(String email);

}
