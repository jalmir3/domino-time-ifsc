package sistema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sistema.model.GameGroup;

import java.util.Optional;
import java.util.UUID;

public interface GameGroupRepository extends JpaRepository<GameGroup, UUID> {
    Optional<GameGroup> findByAccessCode(String accessCode);
}
