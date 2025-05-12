package sistema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sistema.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByActivationToken(String activationToken);

    Optional<User> findByPasswordResetToken(String passwordResetToken);
}