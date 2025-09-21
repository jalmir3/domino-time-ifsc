package sistema.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sistema.model.User;
import java.util.Optional;
import java.util.UUID;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByActivationToken(String activationToken);
    Optional<User> findByPasswordResetToken(String passwordResetToken);
    Optional<User> findById(UUID id);
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailAndDeletedFalse(@Param("email") String email);
    @Query("SELECT u FROM User u WHERE u.nickname = :nickname")
    Optional<User> findByNicknameAndDeletedFalse(@Param("nickname") String nickname);
}