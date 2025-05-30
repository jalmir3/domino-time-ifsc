package sistema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sistema.model.GameGroup;
import sistema.model.GroupPlayer;
import sistema.model.User;

import java.util.UUID;

public interface GroupPlayerRepository extends JpaRepository<GroupPlayer, UUID> {
    boolean existsByUserAndGroup(User user, GameGroup group);
}